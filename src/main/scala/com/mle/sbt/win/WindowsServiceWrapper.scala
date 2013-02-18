package com.mle.sbt.win


object WindowsServiceWrapper{
  def conf(appName:String)={
    <service>
      <id>{appName}</id>
      <name>{appName}</name>
      <description>{appName}</description>
      <executable>%BASE%\{appName}.bat</executable>
      <startargument>start</startargument>
      <stopargument>stop</stopargument>
    </service>
  }
  /**
   *  <logpath>C:\</logpath>
      <logmode>roll</logmode>
   */
}