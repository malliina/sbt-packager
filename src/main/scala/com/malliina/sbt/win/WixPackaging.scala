package com.malliina.sbt.win

import com.malliina.sbt.GenericKeys.{appIcon, displayName}
import com.malliina.sbt.{GenericKeys, WixUtils}
import com.malliina.sbt.win.WinKeys._
import com.typesafe.sbt.SbtNativePackager._
import sbt.Keys._
import sbt._

import scala.xml.NodeSeq

/** You need to set the "WIX" environment variable to the wix installation dir e.g. program files\wix. Use Wix 3.7 or newer.
  *
  * Understanding any of this requires knowledge of WiX. The short story is that this is a complete fucking mess.
  */
object WixPackaging {
  val windowsKeys = com.typesafe.sbt.packager.Keys

  def foldEmpty[T](opt: Option[T])(f: T => NodeSeq) = opt.map(f) getOrElse NodeSeq.Empty

  val wixSettings: Seq[Setting[_]] = inConfig(Windows)(
    Seq(
      windowsKeys.wixConfig := {
        streams.value.log info s"Display name: ${displayName.value}"
        val svcConf = serviceConf.value
        val serviceFragments = svcConf
          .map(s => ServiceFragments.fromConf(s, displayName.value))
          .getOrElse(ServiceFragments.Empty)
        val wixable =
          svcConf
            .map { conf =>
              val excluded = Seq(conf.runtimeConf, conf.xmlConf)
              msiMappings.value.filter {
                case (_, dest) =>
                  !excluded.exists(p => p.getFileName.toString == dest.getFileName.toString)
              }
            }
            .getOrElse {
              msiMappings.value
            }
        val msiFiles = WixUtils.wix(wixable, streams.value)

        val interactiveElement =
          foldEmpty(Option(interactiveInstallation.value).filter(_ == true)) { _ =>
            <UIRef Id="WixUI_FeatureTree"/>
          }

        // To prevent a reboot request before uninstallation, stop the service manually.
        // ServiceControl doesn't cut it if interactiveInstallation is enabled.

        // It is illegal to schedule a deferred custom action before InstallValidate so this must be immediate (=> dos prompt shown).
        // See http://stackoverflow.com/questions/320921/how-to-add-a-wix-custom-action-that-happens-only-on-uninstall-via-msi.
        // We want to run this action on uninstalls and upgrades. REMOVE="ALL" is true for uninstalls, modifications
        // and upgrades. Seems good enough.
        // <![CDATA[(REMOVE="ALL")]]>
        val stopAppFragment =
          foldEmpty(serviceConf.value.filter(_ => forceStopOnUninstall.value))(_ => {
            (<CustomAction Id="StopService"
                         FileKey={batPath.value.getFileName.toString}
                         ExeCommand="stop"
                         Impersonate="yes"
                         Return="ignore" />
            <InstallExecuteSequence>
              <Custom Action="StopService"
                      Before="InstallValidate"><![CDATA[(REMOVE="ALL")]]></Custom>
            </InstallExecuteSequence>)
          })

        // shows icon in add/remove programs section of control panel
        val iconFragment = foldEmpty(appIcon.value)(icon => {
          (<Icon Id="icon.ico" SourceFile={icon.toAbsolutePath.toString}/>
              <Property Id="ARPPRODUCTICON" Value="icon.ico"/>)
        })

        val postUrlFragment = foldEmpty(postInstallUrl.value)(OpenBrowserWix.forUrl)

        val exeComp = NodeSeq.Empty
        val exeCompRef = NodeSeq.Empty
        val minJavaFragment = foldEmpty(minJavaVersion.value)(v => {
          val spec = "Installed OR (JAVA_CURRENT_VERSION32 >= \"1." + v + "\" OR JAVA_CURRENT_VERSION64 >= \"1." + v + "\")"
          (<Property Id="JAVA_CURRENT_VERSION32">
            <RegistrySearch Id="JRE_CURRENT_VERSION_REGSEARCH32"
                            Root="HKLM" Key="SOFTWARE\JavaSoft\Java Runtime Environment"
                            Name="CurrentVersion"
                            Type="raw"
                            Win64="no" />
          </Property>
          <Property Id="JAVA_CURRENT_VERSION64">
            <RegistrySearch Id="JRE_CURRENT_VERSION_REGSEARCH64"
                            Root="HKLM" Key="SOFTWARE\JavaSoft\Java Runtime Environment"
                            Name="CurrentVersion"
                            Type="raw"
                            Win64="yes" />
          </Property>
          <Condition Message={
            "Java " + v + " is required but not found. Please visit www.java.com to install Oracle Java " + v + " or later, then try again."
          }>{scala.xml.Unparsed("<![CDATA[%s]]>".format(spec))}</Condition>)
        })

        (<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
          <Product Name={displayName.value}
                   Id={productGuid.value}
                   UpgradeCode={upgradeGuid.value}
                   Language='1033'
                   Version={version.value}
                   Manufacturer={GenericKeys.manufacturer.value}>
            <Package Description={displayName.value + " package."}
                     Comments='Windows installer.'
                     Manufacturer={GenericKeys.manufacturer.value}
                     InstallScope='perMachine'
                     InstallerVersion='200'
                     Compressed='yes'/>
            <Upgrade Id={upgradeGuid.value}>
              <UpgradeVersion OnlyDetect='no'
                              Property='PREVIOUSFOUND'
                              Minimum={minUpgradeVersion.value}
                              IncludeMinimum='yes'
                              Maximum={version.value}
                              IncludeMaximum='no'/>
            </Upgrade>
            {iconFragment}
            {minJavaFragment}
            {postUrlFragment}

          <Media Id='1' Cabinet={name.value + ".cab"} EmbedCab='yes'/>

            <Directory Id='TARGETDIR' Name='SourceDir'>
              <Directory Id="DesktopFolder" Name="Desktop"/>
              <Directory Id='ProgramFiles64Folder' Name='PFiles'>
                <Directory Id='INSTALLDIR' Name={displayName.value}>
                  {msiFiles.comp}
                  {exeComp}
                  {serviceFragments.components}
                </Directory>
              </Directory>
            </Directory>
            <Feature Id='Complete'
                     Title={displayName.value + " application"}
                     Description={"The Windows installation of " + displayName.value}
                     Display='expand'
                     Level='1'
                     ConfigurableDirectory='INSTALLDIR'>
              <Feature Id='CoreApp'
                       Title='Core Application'
                       Description='The core application.'
                       Level='1'
                       Absent='disallow'>
                {exeCompRef}
                {msiFiles.ref}
              </Feature>
              {serviceFragments.feature}
            </Feature>
            <MajorUpgrade AllowDowngrades="no"
                          Schedule="afterInstallInitialize"
                          DowngradeErrorMessage="A later version of [ProductName] is already installed. Setup will now exit."/>
            {interactiveElement}
            <UIRef Id="WixUI_ErrorProgressText"/>
            <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
            <WixVariable Id="WixUILicenseRtf" Value={licenseRtf.value.toAbsolutePath.toString}/>
            {stopAppFragment}
          </Product>
        </Wix>)
      },
      windowsKeys.candleOptions ++= Seq("-arch", "x64"),
      windowsKeys.lightOptions ++= Seq("-cultures:en-us") //  "-ext", "WixUtilExtension","-ext", "WixUIExtension",
    )
  )
}
