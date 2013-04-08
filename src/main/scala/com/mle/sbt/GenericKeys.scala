package com.mle.sbt

import java.nio.file.Path
import sbt._

object GenericKeys extends Plugin {
  // These string literals double as directory names on both source and destination
  val confDir = "config"
  val libDir = "lib"
  val scriptDir = "scripts"
  val logDir = "logs"
  val configSrcDir = SettingKey[Path]("config-src-dir", "Config file source dir")
  val configDestDir = SettingKey[Path]("config-dest-dir", "Config file destination dir")
  val libDestDir = SettingKey[Path]("lib-dest-dir", "Destination dir for libraries, typically app_home/lib")
  val configFiles = TaskKey[Seq[Path]]("config-files", "Config files to package with the app")
  val basePath = SettingKey[Path]("base-path", "Same as base-directory")
  val pkgHome = SettingKey[Path]("pkg-home", "Packaging home directory")
  val appJar = TaskKey[Path]("app-jar", "The application jar")
  val appJarName = SettingKey[String]("app-jar-name", "Main app jar on destination")
  val homeVar = SettingKey[String]("home-var", "Application home environment variable")
  val libs = TaskKey[Seq[Path]]("libs", "All (managed and unmanaged) libs")
  val printLibs = TaskKey[Unit]("print-libs", "Prints library .jars to stdout")
  val mappingsPrint = TaskKey[Unit]("print-mappings", "Prints the packaging mappings")
  // identity
  val manufacturer = SettingKey[String]("manufacturer", "Manufacturer (for MSI) and default vendor (for RPM)")
  val confFile = SettingKey[Option[Path]]("conf-file", "Configuration file")
  val pathMappings = TaskKey[Seq[(Path, Path)]]("path-mappings", "File mappings")
  val targetPath = SettingKey[Path]("target-path", "Target as a Path")
  val versionFile = SettingKey[Path]("version-file", "Version file (written upon packaging)")
  val deployFiles = TaskKey[Seq[Path]]("deploy-files", "Files installed")
  val printFiles = TaskKey[Unit]("files", "Prints the installed files")
  val logger = TaskKey[Logger]("logger", "Logger helper")
}