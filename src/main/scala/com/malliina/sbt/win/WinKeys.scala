package com.malliina.sbt.win

import java.nio.file.Path

import com.malliina.file.StorageFile
import com.malliina.sbt.PackagingUtil
import sbt.{Logger, settingKey, taskKey}

import scala.xml.Elem

object WinKeys {
  /**
   * Source paths
   */
  val launch4jcExe = settingKey[Path]("Path to launch4jc.exe")
  val winSwExe = settingKey[Path]("Windows Service Wrapper .exe path")
  val licenseRtf = settingKey[Path]("Path to license RTF for Windows. Shown to the user during installation.")
  val batPath = settingKey[Path]("Application .bat")
  /**
   * Other
   */
  val msiName = settingKey[String]("Name of MSI package built with task win")
  val win = taskKey[Path]("Verifies settings followed by package-msi")
  val msi = taskKey[Path]("Shortcut to windows:win (oh yes)")
  val printPaths = taskKey[Seq[Path]]("Prints the paths to the required files for MSI packaging")
  val exePath = settingKey[Path]("Application .exe path on windows during packaging")
  val windowsJarPath = settingKey[Path]("Path to jar on windows during packaging")
  val launch4jcConf = settingKey[Path]("Path to launch4j XML configuration file")
  val winSwConf = settingKey[Path]("Windows Service Wrapper .xml path on build machine")
  val winSwExeName = settingKey[String]("Windows Service Wrapper executable name on target")
  val winSwConfName = settingKey[String]("Windows Service Wrapper XML config file name on target")
  val winSwName = settingKey[String]("Windows Service Wrapper name on target")
  val runtimeConfTargetPath = settingKey[Path]("Path to winsw.exe.config after build")
  val winSwXmlTargetPath = settingKey[Path]("Path to winsw.xml after build")
  val serviceConf = settingKey[Option[ServiceConf]]("Winsw confs")
  val forceStopOnUninstall = settingKey[Boolean]("If true, stops the service before uninstallation using an ugly custom action. Defaults to true.")
  val productGuid = settingKey[String]("Product GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val upgradeGuid = settingKey[String]("Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val shortcut = settingKey[Boolean]("Whether or not to install a desktop shortcut to the main application executable")
  val serviceImplementation = settingKey[Option[ServiceImplementation]]("Service impl (winsw or mle)")
  val msiMappings = taskKey[Seq[(Path, Path)]]("File mappings for MSI packaging")
  val uuid = taskKey[String]("Generates a new GUID using UUID.randomUUID().")
  val minUpgradeVersion = settingKey[String]("The minimum version from which to upgrade.")
  val minJavaVersion = settingKey[Option[Int]]("The minimum required preinstalled Java version, if any. Examples: 6, 7, 8.")
  val postInstallUrl = settingKey[Option[String]]("URL to open after installation.")
  val interactiveInstallation = settingKey[Boolean]("True if the MSI-installer should be interactive, false otherwise. If true, the installer will prompt for reboots when upgrading, if the service is running. I don't know why.")

  sealed trait ServiceImplementation {
    def prepare(log: Logger, targetFilePath: Path, n: String, dN: String, swConfName: String, confDest:Path): Unit = ()
  }

  case object Winsw extends ServiceImplementation {
    override def prepare(log: Logger, targetFilePath: Path, n: String, dN: String, swConfName: String, confDest:Path): Unit = {
      log.info("Creating service wrapper")
      // build winsw service wrapper XML configuration file
      def toFile(xml: Elem, file: Path) {
        PackagingUtil.writerTo(file)(_.println(xml.toString()))
        log.info("Created: " + file.toAbsolutePath)
      }

      val conf = WindowsServiceWrapper.conf(n, dN)
      val confFile = targetFilePath / swConfName
      toFile(conf, confFile)

      val runtimeConf = WindowsServiceWrapper.netRuntimeConf
      toFile(runtimeConf, confDest)
    }
  }

  case object KingMichaelImplementation extends ServiceImplementation

}

