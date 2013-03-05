package com.mle.sbt.unix

import sbt.{TaskKey, SettingKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object UnixKeys {
  val scriptPath = SettingKey[Option[Path]]("script-path", "Location of scripts (if any)")
  val scriptFiles = TaskKey[Seq[Path]]("scripts", "Scripts to package with the app")
}
