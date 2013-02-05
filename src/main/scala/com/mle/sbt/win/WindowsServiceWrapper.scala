package com.mle.sbt.win

import java.nio.file.Path

object WindowsServiceWrapper{
  def conf(appName:String,batFile:Path,winswExe:Path,homeVar:String)={

    <service>
      <id>{appName}</id>
      <name>{appName}</name>
      <description>{appName}</description>
      <executable>%{homeVar}%\{appName}.bat</executable>
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
    val baseCommand =       """"""" + "[INSTALLDIR]" + winswExeName + """"""" + " "
    val installCommand = baseCommand + "install"
    val uninstallCommand = baseCommand + "uninstall"
//    val stopCommand = """"""" + winswExeName + """"""" + " stop"
    val stopCommand = baseCommand + "stop"
    (
        <CustomAction Id="QuietServiceInstall_Cmd" Property="QuietServiceInstall"
                    Value={installCommand} Execute="immediate"/>
        <CustomAction Id="QuietServiceInstall" BinaryKey="WixCA" DllEntry="CAQuietExec"
                      Execute="deferred" Return="check" Impersonate="no"/>
        <Property Id="QtExecCmdLine" Value={stopCommand}/>
        <CustomAction Id="QuietServiceStop" BinaryKey="WixCA" DllEntry="CAQuietExec"
                      Execute="immediate" Return="check"/>
      <InstallExecuteSequence>
        <Custom Action="QuietServiceInstall_Cmd" Before="QuietServiceInstall"/>
        <Custom Action="QuietServiceInstall" After="InstallFiles">NOT Installed</Custom>
        <Custom Action="QuietServiceStop" Before="InstallValidate">REMOVE="ALL"</Custom>
      </InstallExecuteSequence>
      )
  }

//    <Custom Action="QuietServiceStop_Cmd" Before="QuietServiceStop"/>
  //      <Custom Action="QuietServiceUninstall_Cmd" Before="QuietServiceUninstall"/>
  //      <Custom Action="QuietServiceUninstall" Before="RemoveFiles">REMOVE="ALL"</Custom>
  //      <CustomAction Id="QuietServiceUninstall_Cmd" Property="QuietServiceUninstall"
  //                    Value={uninstallCommand} Execute="immediate"/>
  //        <CustomAction Id="QuietServiceUninstall" BinaryKey="WixCA" DllEntry="CAQuietExec"
  //                      Execute="deferred" Return="check" Impersonate="no"/>
//    <CustomAction Id="QuietServiceUninstall_Cmd" Property="QuietServiceUninstall"
//                  Value={uninstallCommand} Execute="immediate"/>
//      <CustomAction Id="QuietServiceUninstall" BinaryKey="WixCA" DllEntry="CAQuietExec"
//                    Execute="deferred" Return="check" Impersonate="no"/>
//    <Custom Action="QuietServiceStop_Cmd" Before="QuietServiceStop"/>
//    <Custom Action="QuietServiceStop" After="InstallInitialize">REMOVE="ALL"</Custom>
  //         (<CustomAction Id="ServiceInstall"
  //                        FileKey={winswExeName}
  //                        ExeCommand="install"
  //                        Execute="deferred"
  //                        Return="check"
  //                        HideTarget="no"
  //                        Impersonate="no"/>
  //         <CustomAction Id="ServiceUninstall"
  //                       FileKey={winswExeName}
  //                       ExeCommand="uninstall"
  //                       Execute="deferred"
  //                       Return="check"
  //                       HideTarget="no"
  //                       Impersonate="no"/>
  //           <InstallExecuteSequence>
  //             <Custom Action="ServiceInstall" Before="InstallFinalize">NOT Installed</Custom>
  //             <Custom Action="ServiceUninstall" Before="RemoveFiles">REMOVE="ALL"</Custom>
  //           </InstallExecuteSequence>
  //          )


  //    (   <Property Id="QuietServiceInstall" Value={installCommand}/>
  //        <CustomAction Id="QuietServiceInstall"
  //                      BinaryKey="WixCA"
  //                      DllEntry="CAQuietExec"
  //                      Execute="deferred"
  //                      Return="check"
  //                      HideTarget="no"
  //                      Impersonate="no"/>
  //        <Property Id="QuietServiceUninstall" Value={uninstallCommand}/>
  //        <CustomAction Id="QuietServiceUninstall"
  //                      BinaryKey="WixCA"
  //                      DllEntry="CAQuietExec"
  //                      Execute="deferred"
  //                      Return="check"
  //                      HideTarget="no"
  //                      Impersonate="no"/>
  //      <InstallExecuteSequence>
  //        <Custom Action="QuietServiceInstall" Before="InstallFinalize">NOT Installed</Custom>
  //        <Custom Action="QuietServiceUninstall" Before="RemoveFiles">REMOVE="ALL"</Custom>
  //      </InstallExecuteSequence>
  //      )

  //      <Custom Action="QuietServiceUninstall_Cmd" Before="QuietServiceUninstall"/>
  //      <Custom Action="QuietServiceUninstall" Before="RemoveFiles">REMOVE="ALL"</Custom>
  //      <CustomAction Id="QuietServiceUninstall_Cmd" Property="QuietServiceUninstall"
  //                    Value={uninstallCommand} Execute="immediate"/>
  //        <CustomAction Id="QuietServiceUninstall" BinaryKey="WixCA" DllEntry="CAQuietExec"
  //                      Execute="deferred" Return="check" Impersonate="no"/>
}