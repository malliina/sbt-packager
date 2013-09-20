package com.mle.sbt.win

import WinKeys._
import com.typesafe.sbt.packager._
import com.typesafe.sbt.SbtNativePackager._
import sbt.Keys._
import sbt._
import xml.NodeSeq
import com.mle.sbt.{WixUtils, GenericKeys}

/**
 * Need to set the "WIX" environment variable to the wix installation dir e.g. program files\wix. Use Wix 3.7 or newer.
 */
object WixPackaging extends Plugin {
  val wixSettings: Seq[Setting[_]] = inConfig(Windows)(Seq(
    windows.Keys.wixConfig := {
        GenericKeys.logger.value.info("Display name: " + displayName.value)
        val msiFiles = WixUtils.wix(msiMappings.value)
        val serviceFragments = serviceConf.value.map(s => ServiceFragments.fromConf(s, displayName.value))
          .getOrElse(ServiceFragments.Empty)
        // to prevent a reboot request before uninstallation, stop the service manually. ServiceControl doesn't cut it.
        val stopAppFragment = serviceConf.value.map(_ => {
          // it is illegal to schedule a deferred custom action before InstallValidate so this must be immediate (=> dos prompt shown)
          (<CustomAction ExeCommand="stop" FileKey={batPath.value.getFileName.toString} Id="StopService" Impersonate="yes" Return="ignore" />
            <InstallExecuteSequence>
              <Custom Action="StopService" Before="InstallValidate"><![CDATA[(NOT UPGRADINGPRODUCTCODE) AND (REMOVE="ALL")]]></Custom>
            </InstallExecuteSequence>)
        }).getOrElse(NodeSeq.Empty)
        val postUrlFragment = postInstallUrl.value.map(OpenBrowserWix.forUrl).getOrElse(NodeSeq.Empty)
        // TODO
        val exeComp = NodeSeq.Empty
        val exeCompRef = NodeSeq.Empty
        val minJavaFragment = minJavaVersion.value.map(v => {
          val spec = "Installed OR (JAVA_CURRENT_VERSION32 >= \"1."+v+"\" OR JAVA_CURRENT_VERSION64 >= \"1."+v+"\")"
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
          <Condition Message={"Java "+v+" is required but not found. Please visit www.java.com to install Oracle Java "+v+" or later, then try again."}>{scala.xml.Unparsed("<![CDATA[%s]]>".format(spec))}</Condition>)
        }).getOrElse(NodeSeq.Empty)

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
              <UpgradeVersion OnlyDetect='no' Property='PREVIOUSFOUND'
                              Minimum={minUpgradeVersion.value}
                              IncludeMinimum='yes'
                              Maximum={version.value}
                              IncludeMaximum='no'/>
            </Upgrade>

            {minJavaFragment}
            {postUrlFragment}

          <Media Id='1' Cabinet={name.value + ".cab"} EmbedCab='yes'/>

            <Directory Id='TARGETDIR' Name='SourceDir'>
              <Directory Id="DesktopFolder" Name="Desktop"/>
              <Directory Id='ProgramFilesFolder' Name='PFiles'>
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
            <UIRef Id="WixUI_FeatureTree"/>
            <UIRef Id="WixUI_ErrorProgressText"/>
            <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
            <WixVariable Id="WixUILicenseRtf" Value={licenseRtf.value.toAbsolutePath.toString}/>
            {stopAppFragment}
          </Product>
        </Wix>)
      },
    windows.Keys.lightOptions ++= Seq("-cultures:en-us") //  "-ext", "WixUtilExtension","-ext", "WixUIExtension",
  ))

  /**
   *
   *
   * <Component Id='AppLauncherPath' Guid='24241F02-194C-4AAD-8BD4-379B26F1C661'>
    <Environment Id="PATH" Name="PATH" Value="[INSTALLDIR]" Permanent="no" Part="last" Action="set" System="yes"/>
      </Component>
    <Component Id='HomePath' Guid='24241F02-194C-4AAD-8BD4-379B26F1C661'>
      <Environment Id="PimpHome" Name="PIMPHOME" Value="[INSTALLDIR]" Permanent="no" Part="last" Action="set" System="yes"/>
    </Component>
    <Feature Id='ConfigurePath'
                   Title={"Add " + dispName + " to Windows system PATH"}
                   Description={"This will append " + dispName + " to your Windows system path."}
                   Level='1'>
            <ComponentRef Id='AppLauncherPath'/>
          </Feature>

   <Component Id='HomePathEnvironment' Guid={UUID.randomUUID().toString}>
                    <Environment Id="HomePath" Name={appName.toUpperCase} Value="[INSTALLDIR]" Permanent="no" Part="last" Action="set" System="yes"/>
                    <CreateFolder/>
                  </Component>

   <ComponentRef Id='HomePathEnvironment'/>
                   <ComponentRef Id='ApplicationExecutable'/>


                                                      <Component Id='ApplicationExecutable' Guid='*'>
                  <File Id='app_exe' Name={exeFileName} DiskId='1' Source={exe.toAbsolutePath.toString} KeyPath="yes">
                    {shortcutFragment}
                  </File>
                </Component>
   val shortcutFragment = ifSelected(desktopShortcut) {
            <Shortcut Id='desktopShortcut' Directory='DesktopFolder' Name={dispName}
                      WorkingDirectory='INSTALLDIR' Icon={exeFileName} IconIndex="0" Advertise="yes"/>
        }

    <Icon Id={exeFileName} SourceFile={icon.toAbsolutePath.toString}/>
            <Property Id="ARPPRODUCTICON" Value={exeFileName}/>
   */

}