package com.malliina.sbt.unix

import sbt.{taskKey, settingKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object UnixKeys {
  val scriptPath = settingKey[Option[Path]]("Location of scripts (if any)")
  val scriptFiles = taskKey[Seq[Path]]("Scripts to package with the app")
  // Tasks
  val libMappings = taskKey[Seq[(Path, Path)]]("Libs mapped to paths")
  val confMappings = taskKey[Seq[(Path, Path)]]("Confs mapped to paths")
  val scriptMappings = taskKey[Seq[(Path, Path)]]("Scripts mapped to paths")

  /**
   * Destination keys
   */
  val unixHome = settingKey[Path]("Home dir on unix")
  val unixLibDest = settingKey[Path]("Lib dir on unix")
  val unixScriptDest = settingKey[Path]("Script dir on unix")
  val unixLogDir = settingKey[Path]("Log dir on unix")
}
