package com.mle.sbt

import java.nio.file.Path
import sbt._

object GenericKeys extends Plugin{
  // These string literals double as directory names on both source and destination
  val confDir = "conf"
  val libDir = "lib"
  val scriptDir = "scripts"
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
}