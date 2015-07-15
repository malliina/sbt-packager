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

  // for play
  val httpPort = settingKey[Option[Int]]("http.port")
  val httpsPort = settingKey[Option[Int]]("https.port")
  val pidFile = settingKey[Option[Path]]("PID file path")
  val appHome = settingKey[Option[Path]]("app home dir on destination")
  val playConf = settingKey[Option[LinuxPlayConf]]("play configuration")

  /**
   * Misc
   */
  val printPaths = taskKey[Seq[Path]]("Prints unix packaging paths")
}
