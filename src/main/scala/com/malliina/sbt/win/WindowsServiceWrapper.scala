package com.malliina.sbt.win

object WindowsServiceWrapper {
    def conf(appName: String, displayName: String)=
      (
      <service>
        <id>{displayName}</id>
        <name>{displayName}</name>
        <description>{displayName}</description>
        <executable>%BASE%\{appName}.bat</executable>
        <startargument>start</startargument>
        <stopexecutable>%BASE%\{appName}.bat</stopexecutable>
        <stopargument>stop</stopargument>
        <logpath>%SystemDrive%\ProgramData\{appName}\logs</logpath>
      </service>
    )
  def netRuntimeConf =
    (<configuration>
      <startup>
        <supportedRuntime version="v2.0.50727" />
        <supportedRuntime version="v4.0" />
      </startup>
    </configuration>)
  def conf2(appName: String) =
    "start=" + appName + ".bat\n" +
      "startParam=silent\n" +
      "stop=" + appName + ".bat\n" +
      "stopParam=stop\n" +
      "name="+appName
}
