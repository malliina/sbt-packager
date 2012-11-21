package com.mle.sbt.unix

import sbt.{TaskKey, SettingKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object UnixKeys {
  val unixPkgHome = SettingKey[Path]("unix-pkg-home", "Source directory for files native to unix")
  val configPath = SettingKey[Option[Path]]("config-path", "Location of config files (if any)")
  val scriptPath = SettingKey[Option[Path]]("script-path", "Location of scripts (if any)")
  val configFiles = TaskKey[Seq[Path]]("config-files", "Config files to package with the app")
  val scriptFiles = TaskKey[Seq[Path]]("scripts", "Scripts to package with the app")
}
