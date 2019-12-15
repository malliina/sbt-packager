package com.malliina.sbt.win

import java.nio.file.Path

import com.malliina.sbt.PackagingUtil
import sbt.{Logger, settingKey, taskKey}

import scala.xml.Elem

object WinKeys {

  /** Source paths
    */
  val launch4jcExe = settingKey[Path]("Path to launch4jc.exe")
  val winSwExe = settingKey[Path]("Windows Service Wrapper .exe path")
  val licenseRtf =
    settingKey[Path]("Path to license RTF for Windows. Shown to the user during installation.")
  val batPath = settingKey[Path]("Application .bat")

  /** Other
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
  val winSwStartExecutable = settingKey[String]("Executable to start")
  val winSwStopExecutable = settingKey[String]("Executable to stop")
  val winSwStartArgument = settingKey[String]("Argument to start")
  val winSwStopArgument = settingKey[String]("Argument to stop")
  val winSwLogPath = settingKey[String]("Windows Service Wrapper log path, i.e. the logpath value")
  val runtimeConfTargetPath = settingKey[Path]("Path to winsw.exe.config after build")
  val winSwXmlTargetPath = settingKey[Path]("Path to winsw.xml after build")
  val serviceConf = settingKey[Option[ServiceConf]]("Winsw confs")
  val forceStopOnUninstall = settingKey[Boolean](
    "If true, stops the service before uninstallation using an ugly custom action. Defaults to true."
  )
  val productGuid =
    settingKey[String]("Product GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val upgradeGuid =
    settingKey[String]("Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val shortcut = settingKey[Boolean](
    "Whether or not to install a desktop shortcut to the main application executable"
  )
  val serviceImplementation =
    settingKey[Option[ServiceImplementation]]("Service impl (winsw or mle)")
  val msiMappings = taskKey[Seq[(Path, Path)]]("File mappings for MSI packaging")
  val uuid = taskKey[String]("Generates a new GUID using UUID.randomUUID().")
  val minUpgradeVersion = settingKey[String]("The minimum version from which to upgrade.")
  val minJavaVersion = settingKey[Option[Int]](
    "The minimum required preinstalled Java version, if any. Examples: 6, 7, 8."
  )
  val postInstallUrl = settingKey[Option[String]]("URL to open after installation.")
  val interactiveInstallation = settingKey[Boolean](
    "True if the MSI-installer should be interactive, false otherwise. If true, the installer will prompt for reboots when upgrading, if the service is running. I don't know why."
  )
  val useTerminateProcess = settingKey[Boolean](
    "True to use the TerminateProcess API to stop a service, false for custom stop implementations"
  )
  val stopParentProcessFirst = settingKey[Boolean]("WinSW configuration parameter")
  val preparePackaging = taskKey[Seq[Path]]("Generate any necessary files for packaging.")

  sealed trait ServiceImplementation {
    def prepare(winswConf: WinswConf, confDest: Path, runtimeDest: Path, log: Logger): Seq[Path]
  }

  case object Winsw extends ServiceImplementation {
    def prepare(winswConf: WinswConf, confDest: Path, runtimeDest: Path, log: Logger): Seq[Path] = {
      def writeXml(xml: Elem, file: Path): Unit = {
        PackagingUtil.writerTo(file)(_.println(xml.toString()))
        log.info(s"Created: ${file.toAbsolutePath}")
      }

      writeXml(WindowsServiceWrapper.conf(winswConf), confDest)
      writeXml(WindowsServiceWrapper.netRuntimeConf, runtimeDest)
      Seq(confDest, runtimeDest)
    }
  }

  case object KingMichaelImplementation extends ServiceImplementation {
    override def prepare(
      winswConf: WinswConf,
      confDest: Path,
      runtimeDest: Path,
      log: Logger
    ): Seq[Path] = Nil
  }

}
