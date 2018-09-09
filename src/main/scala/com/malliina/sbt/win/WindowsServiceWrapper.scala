package com.malliina.sbt.win

import scala.xml.Elem

object WindowsServiceWrapper {
  def conf(conf: WinswConf): Elem = conf match {
    case short@ShortWinswConf(_, _, _, _, _) => shortConf(short)
    case full@FullWinswConf(_, _, _, _, _, _, _, _) => fullConf(full)
  }

  def shortConf(conf: ShortWinswConf): Elem =
    (
      <service>
        <id>{conf.displayName}</id>
        <name>{conf.displayName}</name>
        <description>{conf.displayName}</description>
        <executable>{conf.executable}</executable>
        <logpath>{conf.logPath}</logpath>
        <stopparentprocessfirst>{conf.stopParentProcessFirstString}</stopparentprocessfirst>
      </service>
      )

    def fullConf(conf: FullWinswConf): Elem =
      (
      <service>
        <id>{conf.displayName}</id>
        <name>{conf.displayName}</name>
        <description>{conf.displayName}</description>
        <executable>{conf.startExecutable}</executable>
        <startargument>{conf.startArgument}</startargument>
        <stopexecutable>{conf.stopExecutable}</stopexecutable>
        <stopargument>{conf.stopArgument}</stopargument>
        <logpath>{conf.logPath}</logpath>
        <stopparentprocessfirst>{conf.stopParentProcessFirstString}</stopparentprocessfirst>
      </service>
    )
  def netRuntimeConf: Elem =
    (<configuration>
      <startup>
        <supportedRuntime version="v2.0.50727" />
        <supportedRuntime version="v4.0" />
      </startup>
    </configuration>)
}
