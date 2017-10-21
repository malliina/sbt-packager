package com.malliina.sbt.win

object WindowsServiceWrapper {
    def conf(conf: WinswConf)=
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
      </service>
    )
  def netRuntimeConf =
    (<configuration>
      <startup>
        <supportedRuntime version="v2.0.50727" />
        <supportedRuntime version="v4.0" />
      </startup>
    </configuration>)

  case class WinswConf(appName: String,
                       displayName: String,
                       startExecutable: String,
                       startArgument: String,
                       stopExecutable: String,
                       stopArgument: String,
                       logPath: String)
}
