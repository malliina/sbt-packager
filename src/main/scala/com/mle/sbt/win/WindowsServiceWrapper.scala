package com.mle.sbt.win


object WindowsServiceWrapper{
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
    </service>
  )
  /**
   *  <logpath>C:\</logpath>
      <logmode>roll</logmode>
   */
}