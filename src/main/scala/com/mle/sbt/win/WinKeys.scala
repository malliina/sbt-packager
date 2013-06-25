package com.mle.sbt.win

import java.nio.file.Path
import sbt.{TaskKey, SettingKey}

object WinKeys {
  /**
   * Source paths
   */
  val appIcon = SettingKey[Path]("app-icon", "Path to icon (.ico) file for the application on windows")
  val launch4jcExe = SettingKey[Path]("launch4jc-exe", "Path to launch4jc.exe")
  val winSwExe = SettingKey[Path]("winsw-exe", "Windows Service Wrapper .exe path")
  val licenseRtf = SettingKey[Path]("license-rtf", "Path to license RTF for windows. Shown to the user during installation.")
  val batPath = SettingKey[Path]("bat-path", "Application .bat")
  /**
   * Other
   */
  val msiName = SettingKey[String]("msi-name", "Name of MSI package built with task win")
  val win = TaskKey[Path]("win", "Verifies settings followed by package-msi")
  val printPaths = TaskKey[Seq[Path]]("print-win-paths", "Prints the paths to the required files for MSI packaging")
  val verifySettings = TaskKey[Unit]("verify-settings", "Verifies that the required files for MSI packaging exist in the project and that a main class has been specified")
  val displayName = SettingKey[String]("display-name", "Display name of application")
  val exePath = SettingKey[Path]("exe-path", "Application .exe path on windows during packaging")
  val windowsJarPath = SettingKey[Path]("win-jar-path", "Path to jar on windows during packaging")
  val launch4jcConf = SettingKey[Path]("launch4j-conf", "Path to launch4j XML configuration file")
  val winSwConf = SettingKey[Path]("winsw-conf", "Windows Service Wrapper .xml path on build machine")
  val winSwExeName = SettingKey[String]("winsw-exe-name", "Windows Service Wrapper executable name on target")
  val winSwConfName = SettingKey[String]("winsw-conf-name", "Windows Service Wrapper XML config file name on target")
  val winSwName = SettingKey[String]("winsw-name", "Windows Service Wrapper name on target")
  val serviceConf = SettingKey[Option[ServiceConf]]("winsw-container", "Winsw confs")
  val productGuid = SettingKey[String]("product-guid", "Product GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val upgradeGuid = SettingKey[String]("upgrade-guid", "Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val shortcut = SettingKey[Boolean]("shortcut", "Whether or not to install a desktop shortcut to the main application executable")
  val serviceFeature = SettingKey[Boolean]("service-feature", "Whether or not to include the option to install the application as a service")
  val msiMappings = TaskKey[Seq[(Path, Path)]]("msi-mappings", "File mappings for MSI packaging")
  val uuid = TaskKey[String]("uuid", "Generates a new GUID using UUID.randomUUID().")
  val minUpgradeVersion = SettingKey[String]("min-upgrade", "The minimum version from which to upgrade.")
  val minJavaVersion = SettingKey[Option[Int]]("win-min-java", "The minimum required preinstalled Java version, if any. Examples: 6, 7, 8.")
}

