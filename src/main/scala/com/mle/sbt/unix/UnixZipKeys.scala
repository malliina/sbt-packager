package com.mle.sbt.unix

import sbt._
import java.nio.file.Path
import com.mle.sbt.GenericKeys._

/**
 *
 * @author mle
 */
object UnixZipKeys {
  /**
   * Destination
   */
  val outDir = "distrib"
  val libOutDir = outDir + "/" + libDir
  val confOutDir = outDir + "/" + confDir
  val scriptOutDir = outDir + "/" + scriptDir
  val distribDir = SettingKey[Path]("package-dist-dir", "The directory to package dists into")
  val copyLibs = TaskKey[Seq[Path]]("copy-libs", "Copies all (managed and unmanaged) libs to " + libOutDir)
  val copyConfs = TaskKey[Seq[Path]]("copy-confs", "Copies all configuration files to " + confOutDir)
  val copyScripts = TaskKey[Seq[Path]]("copy-scripts", "Copies all configuration files to " + scriptOutDir)
  val createJar = TaskKey[Seq[Path]]("create-jar", "Copies application .jar to " + outDir)
  val packageApp = TaskKey[Seq[Path]]("package-app", "Copies the app (jars, libs, confs) to " + outDir)
  val bat = TaskKey[Seq[Path]]("bat", "Copies the app (jars, libs, confs) along with a .bat file to " + outDir)
  val sh = TaskKey[Seq[Path]]("sh", "Copies the app (jars, libs, confs) along with a .sh file to " + outDir)
  val zip = TaskKey[File]("zip", "Creates a zip of the app to " + outDir)
}
