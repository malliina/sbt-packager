package com.mle.sbt.win

import java.nio.file.Path

object WindowsServiceWrapper{
  def conf(appName:String,batFile:Path,winswExe:Path,homeVar:String)={
    <service>
      <id>{appName}</id>
      <name>{appName} service</name>
      <description>{appName}</description>
      <executable>%{homeVar}%\{appName}.bat</executable>
      <depend>Spooler</depend>
      <startargument>start</startargument>
      <stopargument>stop</stopargument>
    </service>
  }

  /**
   *  <logpath>C:\</logpath>
      <logmode>roll</logmode>
   */

  /**
   * This is dangerous.
   *
   * TODO: create a ServiceInstall fragment instead; see http://avinashkt.blogspot.fi/2007/05/how-to-install-windows-service-using.html
   *
   * @param winswExeName winsw.exe on target
   * @return a wix fragment
   */
  def wixFragment(winswExeName:String) = {
     (<CustomAction Id="ServiceInstall"
                    FileKey={winswExeName}
                    ExeCommand="install"
                    Execute="deferred"
                    Return="check"
                    HideTarget="no"
                    Impersonate="no"/>
         <CustomAction Id="ServiceUninstall"
                       FileKey={winswExeName}
                       ExeCommand="uninstall"
                       Execute="deferred"
                       Return="check"
                       HideTarget="no"
                       Impersonate="no"/>
       <InstallExecuteSequence>
         <Custom Action="ServiceInstall" Before="InstallFinalize">NOT Installed</Custom>
         <Custom Action="ServiceUninstall" Before="RemoveFiles">REMOVE="ALL"</Custom>
       </InstallExecuteSequence>
      )
  }

  /**
   *
   *
  <CustomAction Id="ServiceInstall"
                    FileKey={winswExeName}
                   ExeCommand="install"
                   Execute="deferred"
                   Return="asyncNoWait"/>

   */
}