package com.malliina.sbt.win

sealed trait WinswConf {
  def appName: String

  def displayName: String

  def logPath: String
}

case class FullWinswConf(appName: String,
                         displayName: String,
                         startExecutable: String,
                         startArgument: String,
                         stopExecutable: String,
                         stopArgument: String,
                         logPath: String) extends WinswConf

case class ShortWinswConf(appName: String,
                          displayName: String,
                          executable: String,
                          logPath: String) extends WinswConf
