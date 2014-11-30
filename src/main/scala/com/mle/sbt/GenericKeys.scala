package com.mle.sbt

import java.nio.file.Path
import sbt._

object GenericKeys extends Plugin {
  // These string literals double as directory names on both source and destination
  val confDir = "config"
  val libDir = "lib"
  val scriptDir = "scripts"
  val logDir = "logs"
  val configSrcDir = settingKey[Path]("Config file source dir")
  val configDestDir = settingKey[Path]("Config file destination dir")
  val libDestDir = settingKey[Path]("Destination dir for libraries, typically app_home/lib")
  val configFiles = taskKey[Seq[Path]]("Config files to package with the app")
  val basePath = settingKey[Path]("Same as base-directory")
  val pkgHome = settingKey[Path]("Packaging home directory")
  val appJar = taskKey[Path]("The application jar")
  val appJarName = settingKey[String]("Main app jar on destination")
  val homeVar = settingKey[String]("Application home environment variable")
  val libs = taskKey[Seq[Path]]("All (managed and unmanaged) libs")
  val printLibs = taskKey[Unit]("Prints library .jars to stdout")
  val mappingsPrint = taskKey[Unit]("Prints the packaging mappings")
  // identity
  val manufacturer = settingKey[String]("Manufacturer (for MSI) and default vendor (for RPM)")
  val confFile = settingKey[Option[Path]]("Configuration file")
  val pathMappings = taskKey[Seq[(Path, Path)]]("File mappings")
  val targetPath = settingKey[Path]("Target as a Path")
  val deployFiles = taskKey[Seq[Path]]("Files installed")
  val printFiles = taskKey[Unit]("Prints the installed files")
  val logger = taskKey[Logger]("Logger helper")
  val help = taskKey[Unit]("Shows help")
  val verifySettings = taskKey[Unit]("Verifies that the required files for packaging exist in the project and that a main class has been specified")
  val prepareFiles = taskKey[Seq[Path]]("Copies the files of the app to a target directory (for packaging).")
}