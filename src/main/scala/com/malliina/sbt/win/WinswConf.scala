package com.malliina.sbt.win

sealed trait WinswConf {
  def appName: String
  def displayName: String
  def logPath: String
  def stopParentProcessFirst: Boolean
  def stopParentProcessFirstString: String = if (stopParentProcessFirst) "true" else "false"
}

case class FullWinswConf(
  appName: String,
  displayName: String,
  startExecutable: String,
  startArgument: String,
  stopExecutable: String,
  stopArgument: String,
  logPath: String,
  stopParentProcessFirst: Boolean
) extends WinswConf

case class ShortWinswConf(
  appName: String,
  displayName: String,
  executable: String,
  logPath: String,
  stopParentProcessFirst: Boolean
) extends WinswConf
