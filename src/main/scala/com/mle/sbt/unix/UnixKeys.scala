package com.mle.sbt.unix

import sbt.{taskKey, settingKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object UnixKeys {
  val scriptPath = settingKey[Option[Path]]("Location of scripts (if any)")
  val scriptFiles = taskKey[Seq[Path]]("Scripts to package with the app")
}
