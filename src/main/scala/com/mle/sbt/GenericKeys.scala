package com.mle.sbt

import java.nio.file.Path
import sbt._

object GenericKeys extends Plugin {
  // These string literals double as directory names on both source and destination
  val confDir = "config"
  val libDir = "lib"
  val scriptDir = "scripts"
  val logDir = "logs"
  val configPath = SettingKey[Path]("config-path", "Config file directory")
  val configFiles = TaskKey[Seq[Path]]("config-files", "Config files to package with the app")
  val basePath = SettingKey[Path]("base-path", "Same as base-directory")
  val pkgHome = SettingKey[Path]("pkg-home", "Packaging home directory")
  val appJar = TaskKey[Path]("app-jar", "The application jar")
  val appJarName = SettingKey[String]("app-jar-name", "Main app jar on destination")
  val homeVar = SettingKey[String]("home-var", "Application home environment variable")
  val libs = TaskKey[Seq[Path]]("libs", "All (managed and unmanaged) libs")
  val printLibs = TaskKey[Unit]("print-libs", "Prints library .jars to stdout")
  val printMappings = TaskKey[Unit]("print-mappings", "Prints the packaging mappings")
  // identity
  val manufacturer = SettingKey[String]("manufacturer", "Manufacturer (for MSI) and default vendor (for RPM)")
  val confFile = SettingKey[Option[Path]]("conf-file", "Configuration file")
}