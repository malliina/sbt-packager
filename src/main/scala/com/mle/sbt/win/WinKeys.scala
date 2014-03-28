package com.mle.sbt.win

import java.nio.file.Path
import sbt.{settingKey, taskKey}

object WinKeys {
  /**
   * Source paths
   */
  val appIcon = settingKey[Option[Path]]("App icon (.ico file) on Windows.")
  val launch4jcExe = settingKey[Path]("Path to launch4jc.exe")
  val winSwExe = settingKey[Path]("Windows Service Wrapper .exe path")
  val licenseRtf = settingKey[Path]("Path to license RTF for Windows. Shown to the user during installation.")
  val batPath = settingKey[Path]("Application .bat")
  /**
   * Other
   */
  val msiName = settingKey[String]("Name of MSI package built with task win")
  val win = taskKey[Path]("Verifies settings followed by package-msi")
  val printPaths = taskKey[Seq[Path]]("Prints the paths to the required files for MSI packaging")
  val displayName = settingKey[String]("Display name of application")
  val exePath = settingKey[Path]("Application .exe path on windows during packaging")
  val windowsJarPath = settingKey[Path]("Path to jar on windows during packaging")
  val launch4jcConf = settingKey[Path]("Path to launch4j XML configuration file")
  val winSwConf = settingKey[Path]("Windows Service Wrapper .xml path on build machine")
  val winSwExeName = settingKey[String]("Windows Service Wrapper executable name on target")
  val winSwConfName = settingKey[String]("Windows Service Wrapper XML config file name on target")
  val winSwName = settingKey[String]("Windows Service Wrapper name on target")
  val serviceConf = settingKey[Option[ServiceConf]]("Winsw confs")
  val forceStopOnUninstall = settingKey[Boolean]("If true, stops the service before uninstallation using an ugly custom action. Defaults to true.")
  val productGuid = settingKey[String]("Product GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val upgradeGuid = settingKey[String]("Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val shortcut = settingKey[Boolean]("Whether or not to install a desktop shortcut to the main application executable")
  val serviceFeature = settingKey[Boolean]("Whether or not to include the option to install the application as a service")
  val msiMappings = taskKey[Seq[(Path, Path)]]("File mappings for MSI packaging")
  val uuid = taskKey[String]("Generates a new GUID using UUID.randomUUID().")
  val minUpgradeVersion = settingKey[String]("The minimum version from which to upgrade.")
  val minJavaVersion = settingKey[Option[Int]]("The minimum required preinstalled Java version, if any. Examples: 6, 7, 8.")
  val postInstallUrl = settingKey[Option[String]]("URL to open after installation.")
}

