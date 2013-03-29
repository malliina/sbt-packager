package com.mle.sbt.win

import WindowsKeys._
import com.typesafe.sbt.packager._
import com.typesafe.sbt.SbtNativePackager._
import java.nio.file.Path
import sbt.Keys._
import sbt._
import xml.NodeSeq
import com.mle.sbt.{WixUtils, GenericKeys}

/**
 * The "WIX" environment variable to the wix installation dir e.g. program files\wix. Use Wix 3.7 or newer.
 */
object WixPackaging extends Plugin {
  // need to set the "WIX" environment variable to the wix installation dir e.g. program files\wix. Use Wix 3.7 or newer.
  val wixSettings: Seq[Setting[_]] = inConfig(Windows)(Seq(
    windows.Keys.wixConfig <<= (
      msiMappings, name, version, displayName,
      exePath, licenseRtf, appIcon, productGuid,
      upgradeGuid, shortcut, GenericKeys.manufacturer, serviceConf,
      minUpgradeVersion) map (
      (mappings, appName, appVersion, dispName,
       exe, license, icon, productUid,
       upgradeUid, desktopShortcut, manufact, service,
       minUpVer) => {
        val msiFiles = WixUtils.wix(mappings)
        val exeFileName = exe.getFileName.toString
        val shortcutFragment = ifSelected(desktopShortcut) {
            <Shortcut Id='desktopShortcut' Directory='DesktopFolder' Name={dispName}
                      WorkingDirectory='INSTALLDIR' Icon={exeFileName} IconIndex="0" Advertise="yes"/>
        }
        val serviceFragments = service.map(s => ServiceFragments.fromConf(s, dispName))
          .getOrElse(ServiceFragments.Empty)

        (<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
          <Product Name={dispName}
                   Id={productUid}
                   UpgradeCode={upgradeUid}
                   Language='1033'
                   Version={appVersion}
                   Manufacturer={manufact}>
            <Package Description={dispName + " package."}
                     Comments='Windows installer.'
                     Manufacturer={manufact}
                     InstallScope='perMachine'
                     InstallerVersion='200'
                     Compressed='yes'/>
            <Upgrade Id={upgradeUid}>
              <UpgradeVersion OnlyDetect='no' Property='PREVIOUSFOUND'
                              Minimum={minUpVer} IncludeMinimum='yes'
                              Maximum={appVersion} IncludeMaximum='no'/>

            </Upgrade>
            <Media Id='1' Cabinet={appName + ".cab"} EmbedCab='yes'/>
            <Icon Id={exeFileName} SourceFile={icon.toAbsolutePath.toString}/>
            <Property Id="ARPPRODUCTICON" Value={exeFileName}/>
            <Directory Id='TARGETDIR' Name='SourceDir'>
              <Directory Id="DesktopFolder" Name="Desktop"/>
              <Directory Id='ProgramFilesFolder' Name='PFiles'>
                <Directory Id='INSTALLDIR' Name={dispName}>
                  {msiFiles.compElems}
                  <Component Id='ApplicationExecutable' Guid='*'>
                  <File Id='app_exe' Name={exeFileName} DiskId='1' Source={exe.toAbsolutePath.toString} KeyPath="yes">
                    {shortcutFragment}
                  </File>
                </Component>
                  {serviceFragments.components}
                </Directory>
              </Directory>
            </Directory>
            <Feature Id='Complete'
                     Title={dispName + " application"}
                     Description={"The Windows installation of " + dispName}
                     Display='expand'
                     Level='1'
                     ConfigurableDirectory='INSTALLDIR'>
              <Feature Id='CoreApp'
                       Title='Core Application'
                       Description='The core application.'
                       Level='1'
                       Absent='disallow'>
                <ComponentRef Id='ApplicationExecutable'/>{msiFiles.compRefs}
              </Feature>
              {serviceFragments.feature}
            </Feature>
            <MajorUpgrade AllowDowngrades="no"
                          Schedule="afterInstallInitialize"
                          DowngradeErrorMessage="A later version of [ProductName] is already installed. Setup will now exit."/>
            <UIRef Id="WixUI_FeatureTree"/>
            <UIRef Id="WixUI_ErrorProgressText"/>
            <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
            <WixVariable Id="WixUILicenseRtf" Value={license.toAbsolutePath.toString}/>
          </Product>
        </Wix>)
      }),
    windows.Keys.lightOptions ++= Seq("-ext", "WixUIExtension", "-ext", "WixUtilExtension", "-cultures:en-us")
  ))

  def ifSelected(predicate: Boolean)(onTrue: => NodeSeq) = if (predicate) onTrue else NodeSeq.Empty

  def toWixFragment(mapping: (Path, Path)): WixFile = {
    val (src, dest) = mapping
    val sourcePathAsString = src.toAbsolutePath.toString
    val fileName = dest.getFileName.toString
    val fileId = fileName.replace('-', '_')
    val compId = "comp_" + fileId
    val fragment =
      (<Component Id={compId} Guid='*'>
        <File Id={fileId} Name={fileName} DiskId='1' Source={sourcePathAsString}/>
        <CreateFolder/>
      </Component>)
    WixFile(compId, fragment)
  }

  def toCompRef(refId: String) = {
      <ComponentRef Id={refId}/>
  }

  def toWixFragment(files: Seq[(Path, Path)]): WixFiles = {
    val wixFiles = files.map(toWixFragment)
    val compIds = wixFiles.map(_.compId)
    val compRefFragment = compIds.map(toCompRef).foldLeft(NodeSeq.Empty)(_ ++ _)
    val fragment = wixFiles.map(_.compFragment).foldLeft(NodeSeq.Empty)(_ ++ _)
    WixFiles(compIds, compRefFragment, fragment)
  }

  case class WixFiles(compIds: Seq[String], compRefs: NodeSeq, compsFragment: NodeSeq)

  case class WixFile(compId: String, compFragment: NodeSeq)

  /**
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

   */

}