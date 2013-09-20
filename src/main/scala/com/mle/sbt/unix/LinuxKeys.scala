package com.mle.sbt.unix

import sbt.{settingKey, taskKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object LinuxKeys {
  /**
   * Source keys
   */
  val controlDir = settingKey[Path]("Directory for control files for native packaging")
  val preInstall = settingKey[Path]("Preinstall script")
  val postInstall = settingKey[Path]("Postinstall script")
  val preRemove = settingKey[Path]("Preremove script")
  val postRemove = settingKey[Path]("Postremove script")
  val defaultsFile = settingKey[Path]("The defaults config file, installed to /etc/default/app_name_here")
  val copyrightFile = settingKey[Path]("The copyright file")
  val changelogFile = settingKey[Path]("The changelog file")
  val initScript = settingKey[Path]("Init script for unix")

  /**
   * Misc
   */
  val printPaths = taskKey[Seq[Path]]("Prints unix packaging paths")
  /**
   * Destination keys
   */
  val unixHome = settingKey[Path]("Home dir on unix")
  val unixLibDest = settingKey[Path]("Lib dir on unix")
  val unixScriptDest = settingKey[Path]("Script dir on unix")
  val unixLogDir = settingKey[Path]("Log dir on unix")
  // Tasks
  val libMappings = taskKey[Seq[(Path, String)]]("Libs mapped to paths")
  val confMappings = taskKey[Seq[(Path, String)]]("Confs mapped to paths")
  val scriptMappings = taskKey[Seq[(Path, String)]]("Scripts mapped to paths")
}
