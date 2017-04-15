package com.malliina.sbt.unix

import sbt.{File, settingKey, taskKey}
import java.nio.file.Path

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

  // for play
  val httpPort = settingKey[Option[String]]("http.port, can be 'disabled'")
  val httpsPort = settingKey[Option[String]]("https.port, cannot be 'disabled', use None to disable")
  val pidFile = settingKey[Option[String]]("PID file path")
  val appHome = settingKey[String]("App home dir on destination")

  /**
   * Misc
   */
  val printPaths = taskKey[Seq[Path]]("Prints unix packaging paths")
  val ciBuild = taskKey[File]("Packages and renames the app (for CI).")
}
