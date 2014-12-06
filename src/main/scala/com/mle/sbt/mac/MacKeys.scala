package com.mle.sbt.mac

import java.nio.file.Path

import com.mle.appbundler.{Installer, LaunchdConf, InfoPlistConf}
import sbt._

/**
 * @author mle
 */
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
  val deleteOutOnComplete = settingKey[Boolean]("Delete temp dir after packaging")
  val macAppTarget = settingKey[Path]("Target path to the .app package")
  val app = taskKey[Path]("Creates a .app package")
  val pkg = taskKey[Path]("Creates a .pkg package")
  val dmg = taskKey[Path]("Packages the app to a .dmg file")

  val macPkgRoot = settingKey[Path]("Root folder for OSX packaging")
  val macAppDir = settingKey[Path]("DisplayName.app dir")
  val macContentsDir = settingKey[Path]("Contents dir")
  val macResources = settingKey[Path]("HTML resources for OSX packaging")
  val macScripts = settingKey[Path]("Scripts for OSX packaging")
  val macPkgDir = settingKey[Path]("Temporary package-path for OSX")
  val macDistribution = settingKey[Path]("Distribution.xml for OSX")
  val macPkgBuild = taskKey[Seq[String]]("The /usr/bin/pkgbuild command to run")
  val macProductBuild = taskKey[Seq[String]]("The /usr/bin/productbuild command to run")
  val macPackagePath = settingKey[Path]("The path to the target .pkg for OSX")
  val macPackage = taskKey[Path]("Creates a .pkg for OSX")
}
