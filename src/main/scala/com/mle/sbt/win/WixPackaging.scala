package com.mle.sbt.win

import WindowsKeys._
import com.mle.util.FileUtilities
import com.typesafe.sbt.packager._
import com.typesafe.sbt.SbtNativePackager._
import java.nio.file.{Paths, Files, Path}
import sbt.Keys._
import sbt._
import xml.NodeSeq
import com.mle.sbt.GenericKeys._
import com.mle.sbt.FileImplicits._
import java.io.PrintWriter
import com.mle.sbt.GenericKeys

object WixPackaging extends Plugin {
  def writerTo(path: Path)(op: PrintWriter => Unit) = {
    Files.createDirectories(path.getParent)
    FileUtilities.writerTo(path)(op)
  }

  // need to set the WIX environment variable to the wix installation dir e.g. program files\wix
  val windowsMappings = mappings in windows.Keys.packageMsi in Windows
  val wixSettings: Seq[Setting[_]] = Seq(
    windowsMappings <+= (appJar, appJarName) map (
      (jar, jarName) => jar.toFile -> jarName),
    windowsMappings <++= (appJar, appJarName, name, exePath, mainClass, launch4jcExe, appIcon, target in Windows) map (
      (bin, jarName, appName, exeP, m, l, i, t) => {
        val exeFileName = exeP.getFileName.toString
        val mClass = m.getOrElse(throw new Exception("No mainClass specified; cannot create .exe"))
        val exeFile = Launch4jWrapper.exeWrapper(l, bin, mClass, jarName, t.toPath / "launch4jconf.xml", exeP, i)
        exeFile.toFile -> exeFileName
        Seq.empty[(java.io.File, String)]
      }),
    windowsMappings <++= (name, target in Windows, winSwExe, winSwExeName, winSwConfName, displayName, streams) map (
      (n, t, w, wN, c, d, l) => {
        // build winsw service wrapper XML configuration file
        val conf = WindowsServiceWrapper.conf(n, d)
        val confFile = t.toPath / c
        writerTo(confFile)(_.println(conf.toString()))
        l.log.info("Created: " + confFile.toAbsolutePath)
        //        Seq(confFile.toFile -> c,w.toFile -> wN)
        Seq.empty[(java.io.File, String)]
      }),
    windowsMappings <++= (batPath, name, target in Windows) map ((b, n, t) => {
      val startService = t.toPath / (n + "-start.bat")
      val stopService = t.toPath / (n + "-stop.bat")
      writerTo(startService)(_.println(n + ".bat start"))
      writerTo(stopService)(_.println(n + ".bat stop"))
      Seq(
        b.toFile -> b.getFileName.toString,
        startService.toFile -> startService.getFileName.toString,
        stopService.toFile -> stopService.getFileName.toString)
    }),
    windowsMappings <++= (libs, name) map ((libz, name) =>
      libz.map(libPath => (libPath.toFile -> ("lib/" + libPath.getFileName.toString)))
      ),
    windows.Keys.wixConfig <<= (
      windowsMappings,
      name,
      version in Windows,
      displayName,
      exePath,
      licenseRtf,
      appIcon,
      winSwExe,
      winSwExeName,
      winSwConfName,
      productGuid,
      upgradeGuid,
      shortcut,
      GenericKeys.manufacturer,
      serviceFeature) map (
      (mappings, appName,appVersion, dispName,
       exe, license, icon, serviceExe,
       serviceExeName, serviceConfName, productUid, upgradeUid,
       desktopShortcut, manufact, serviceChoice) => {
        val (libFiles, coreFiles) = mappings.map(kv => (kv._1.toPath -> Paths.get(kv._2))).partition(kv => {
          val parent = kv._2.getParent
          parent != null && parent.getFileName.toString == "lib"
        })
        val libsWixXml = toWixFragment(libFiles)
        val coreFilesXml = toWixFragment(coreFiles)
        val exeFileName = exe.getFileName.toString
        val shortcutFragment = ifSelected(desktopShortcut) {
            <Shortcut Id='desktopShortcut' Directory='DesktopFolder' Name={dispName}
                      WorkingDirectory='INSTALLDIR' Icon={exeFileName} IconIndex="0" Advertise="yes"/>
        }
        val serviceComponents = ifSelected(serviceChoice) {
          <Component Id='ServiceManagerConf' Guid='*'>
            <File Id={serviceConfName.replace('.', '_')} Name={serviceConfName} DiskId='1' Source={serviceConfName}/>
          </Component>
            <Component Id='ServiceManager' Guid='*'>
              <File Id={serviceExeName} Name={serviceExeName} DiskId='1' Source={serviceExe.toAbsolutePath.toString} KeyPath="yes"/>
              <ServiceInstall Id="ServiceInstaller"
                              Type="ownProcess"
                              Vital="yes"
                              Name={appName}
                              DisplayName={dispName}
                              Description={"The " + dispName + " service"}
                              Start="auto"
                              Account="LocalSystem"
                              ErrorControl="ignore"
                              Interactive="no"/>
              <ServiceControl Id="ServiceController" Start="install" Stop="both" Remove="uninstall" Name={appName} Wait="yes"/>
            </Component>
        }
        val serviceFeature = ifSelected(serviceChoice) {
          <Feature Id='InstallAsService'
                   Title={"Install " + dispName + " as a Windows service"}
                   Description={"This will install " + dispName + " as a Windows service."}
                   Level='1'
                   Absent='disallow'>
            <ComponentRef Id='ServiceManager'/>
            <ComponentRef Id='ServiceManagerConf'/>
          </Feature>
        }
        (<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
          <Product Name={dispName}
                   Id={productUid}
                   UpgradeCode={upgradeUid}
                   Language='1033'
                   Version={appVersion}
                   Manufacturer={manufact}>
            <Package Description={dispName + " launcher script."}
                     Comments='Windows installer.'
                     Manufacturer={manufact}
                     InstallScope='perMachine'
                     InstallerVersion='200'
                     Compressed='yes'/>
            <Media Id='1' Cabinet={appName + ".cab"} EmbedCab='yes'/>
            <Icon Id={exeFileName} SourceFile={icon.toAbsolutePath.toString}/>
            <Property Id="ARPPRODUCTICON" Value={exeFileName}/>
            <Directory Id='TARGETDIR' Name='SourceDir'>
              <Directory Id="DesktopFolder" Name="Desktop"/>
              <Directory Id='ProgramFilesFolder' Name='PFiles'>
                <Directory Id='INSTALLDIR' Name={dispName}>
                  <Directory Id="lib_dir" Name="lib">
                    {libsWixXml.compsFragment}
                  </Directory>{coreFilesXml.compsFragment}<Component Id='ApplicationExecutable' Guid='*'>
                  <File Id='app_exe' Name={exeFileName} DiskId='1' Source={exe.toAbsolutePath.toString} KeyPath="yes">
                    {shortcutFragment}
                  </File>
                </Component>{serviceComponents}
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
                <ComponentRef Id='ApplicationExecutable'/>{coreFilesXml.compRefs}{libsWixXml.compRefs}
              </Feature>{serviceFeature}
            </Feature>
            <MajorUpgrade AllowDowngrades="no"
                          Schedule="afterInstallInitialize"
                          DowngradeErrorMessage="A later version of [ProductName] is already installed.  Setup will now exit."/>
            <UIRef Id="WixUI_FeatureTree"/>
            <UIRef Id="WixUI_ErrorProgressText"/>
            <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
            <WixVariable Id="WixUILicenseRtf" Value={license.toAbsolutePath.toString}/>
          </Product>
        </Wix>)
      }),
    windows.Keys.lightOptions ++= Seq("-ext", "WixUIExtension", "-ext", "WixUtilExtension", "-cultures:en-us")
  )

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
    <Feature Id='ConfigurePath'
                   Title={"Add " + dispName + " to Windows system PATH"}
                   Description={"This will append " + dispName + " to your Windows system path."}
                   Level='1'>
            <ComponentRef Id='AppLauncherPath'/>
          </Feature>
   */

}