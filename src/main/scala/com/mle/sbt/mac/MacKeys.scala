package com.mle.sbt.mac

import java.nio.file.Path

import com.mle.appbundler.InfoPlistConf
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
  val infoPlistConf = settingKey[Option[InfoPlistConf]]("The info plist conf: define to override defaults")
  val macAppTarget = settingKey[Path]("Target path to the .app package")
  val app = taskKey[Path]("Creates a .app package")
  val dmg = taskKey[Path]("Packages the app to a .dmg file")
}
