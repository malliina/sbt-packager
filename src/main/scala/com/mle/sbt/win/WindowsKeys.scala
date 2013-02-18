package com.mle.sbt.win

import java.nio.file.Path
import sbt.{TaskKey, SettingKey}

object WindowsKeys {
  val windowsPkgHome = SettingKey[Path]("win-pkg-home", "Windows packaging directory")
  val displayName = SettingKey[String]("display-name","Display name of application")
  val exePath = SettingKey[Path]("exe-path", "Application .exe path on windows during packaging")
  val batPath = SettingKey[Path]("bat-path", "Application .bat path on windows during packaging")
  val windowsJarPath = SettingKey[Path]("win-jar-path", "Path to jar on windows during packaging")
  val licenseRtf = SettingKey[Path]("license-rtf", "Path to license RTF for windows. Shown to the user during installation.")
  val launch4jcExe = SettingKey[Path]("launch4jc-exe", "Path to launch4jc.exe")
  val launch4jcConf = SettingKey[Path]("launch4j-conf", "Path to launch4j XML configuration file")
  val appIcon = SettingKey[Path]("app-icon", "Path to icon (.ico) file for the application on windows")
  val winSwExe = SettingKey[Path]("winsw-exe", "Windows Service Wrapper .exe path")
  val winSwConf = SettingKey[Path]("winsw-conf", "Windows Service Wrapper .xml path on build machine")
  val winSwExeName = SettingKey[String]("winsw-exe-name", "Windows Service Wrapper executable name on target")
  val winSwConfName = SettingKey[String]("winsw-conf-name", "Windows Service Wrapper XML config file name on target")
  val winSwName = SettingKey[String]("winsw-name", "Windows Service Wrapper name on target")
  val verifyPaths = TaskKey[Unit]("verify-paths", "Verifies that the required files for MSI packaging exist in the project")
  val win = TaskKey[sbt.File]("win", "verify-paths followed by package-msi")
  val productGuid = SettingKey[String]("product-guid", "Product GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val upgradeGuid = SettingKey[String]("upgrade-guid", "Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().")
  val shortcut = SettingKey[Boolean]("shortcut","Whether or not to install a desktop shortcut to the main application executable")
}