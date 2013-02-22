package com.mle.sbt.unix

import sbt.{TaskKey, SettingKey}
import java.nio.file.Path

/**
 *
 * @author mle
 */
object LinuxKeys {
  /**
   * Source keys
   */
  val controlDir = SettingKey[Path]("control-dir", "Directory for control files for native packaging")
  val preInstall = SettingKey[Path]("pre-install", "Preinstall script")
  val postInstall = SettingKey[Path]("post-install", "Postinstall script")
  val preRemove = SettingKey[Path]("pre-remove", "Preremove script")
  val postRemove = SettingKey[Path]("post-remove", "Postremove script")
  /**
   * Misc
   */
  val printPaths = TaskKey[Seq[Path]]("print-unix-paths", "Prints unix packaging paths")
  /**
   * Destination keys
   */
  val unixHome = SettingKey[Path]("unix-home", "Home dir on unix")
  val unixLibDest = SettingKey[Path]("unix-lib-home", "Lib dir on unix")
  val unixConfDest = SettingKey[Path]("unix-conf-home", "Conf dir on unix")
  val unixScriptDest = SettingKey[Path]("unix-script-home", "Script dir on unix")
  val unixLogDir = SettingKey[Path]("unix-log-home", "Log dir on unix")
  // Tasks
  val defaultsFile = TaskKey[Path]("defaults-file", "The defaults config file")
  val libMappings = TaskKey[Seq[(Path, String)]]("lib-mappings", "Libs mapped to paths")
  val confMappings = TaskKey[Seq[(Path, String)]]("conf-mappings", "Confs mapped to paths")
  val scriptMappings = TaskKey[Seq[(Path, String)]]("script-mappings", "Scripts mapped to paths")
  val debFiles = TaskKey[Seq[String]]("deb-files", "Files in DEB package")
  val rpmFiles = TaskKey[Seq[String]]("rpm-files", "Files in RPM package")
}
