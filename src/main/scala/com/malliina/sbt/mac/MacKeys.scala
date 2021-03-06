package com.malliina.sbt.mac

import java.nio.file.Path

import com.malliina.appbundler.{FileMapping, InfoPlistConf, Installer, LaunchdConf}
import sbt._

object MacKeys {
  val plistFile = settingKey[Path]("Path to .plist file")
  val appIdentifier = settingKey[String]("Globally unique app ID")
  val embeddedJavaHome = settingKey[Path]("Path to java home which will be embedded in the OSX .app package")
  val jvmOptions = settingKey[Seq[String]]("JVM options for OSX")
  val jvmArguments = settingKey[Seq[String]]("JVM arguments for OSX")
  val hideDock = settingKey[Boolean]("If true, the app is hidden from the dock when running")
  val infoPlistConf = taskKey[InfoPlistConf]("The info plist conf: define to override defaults")
  val launchdConf = settingKey[Option[LaunchdConf]]("The launchd configuration, if any")
  val defaultLaunchd = settingKey[LaunchdConf]("The default launchd configuration, if enabled")
  val installer = taskKey[Installer]("Installer conf")
  val pkgIcon = settingKey[Option[Path]]("Path to the icon of the .pkg file")
  val extraDmgFiles = settingKey[Seq[FileMapping]]("Files to include in the .dmg image, such as .DS_Store and images")
  val deleteOutOnComplete = settingKey[Boolean]("Delete temp dir after packaging")
  val macAppTarget = settingKey[Path]("Target path to the .app package")
  val app = taskKey[Path]("Creates a .app package")
  val pkg = taskKey[Path]("Creates a .pkg installer")
  val dmg = taskKey[Path]("Packages the app to a .dmg file")
}
