package com.malliina.sbt.unix

import com.malliina.sbt.GenericKeys
import sbt._
import java.nio.file.Path
import GenericKeys._

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
  val distribDir = settingKey[Path]("The directory to package dists into")
  val copyLibs = taskKey[Seq[Path]]("Copies all (managed and unmanaged) libs to " + libOutDir)
  val copyConfs = taskKey[Seq[Path]]("Copies all configuration files to " + confOutDir)
  val copyScripts = taskKey[Seq[Path]]("Copies all configuration files to " + scriptOutDir)
  val createJar = taskKey[Seq[Path]]("Copies application .jar to " + outDir)
  val packageApp = taskKey[Seq[Path]]("Copies the app (jars, libs, confs) to " + outDir)
  val bat = taskKey[Seq[Path]]("Copies the app (jars, libs, confs) along with a .bat file to " + outDir)
  val sh = taskKey[Seq[Path]]("Copies the app (jars, libs, confs) along with a .sh file to " + outDir)
  val zip = taskKey[File]("Creates a zip of the app to " + outDir)
}
