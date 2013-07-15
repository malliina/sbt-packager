package com.mle.sbt.win

/**
 *
 * @author mle
 */
object OpenBrowserWix {

  def forUrl(url: String) =
    (<Property Id="MyURL">{scala.xml.Unparsed("<![CDATA[%s]]>".format(url))}</Property>
        <CustomAction Id="SetOpenURL"
                      Property="WixShellExecTarget"
                      Value="[MyURL]" />
        <CustomAction Id="OpenURL"
                      BinaryKey="WixCA"
                      DllEntry="WixShellExec"
                      Impersonate="yes"
                      Return="ignore" />

      <InstallExecuteSequence>
        <!-- Launch webpage during full install, but not upgrade -->
        <Custom Action="SetOpenURL" After="InstallFinalize"><![CDATA[NOT Installed]]></Custom>
        <Custom Action="OpenURL" After="SetOpenURL"><![CDATA[NOT Installed]]></Custom>
      </InstallExecuteSequence>
      )

  val valueNotWorking =
    (<Property Id="BROWSER">
      <RegistrySearch Id='DefaultBrowser'
                      Type='raw'
                      Root='HKCR'
                      Key='http\shell\open\command'/>
    </Property>
        <CustomAction Id='LaunchBrowser'
                      Property='BROWSER'
                      ExeCommand='http://www.google.com'
                      Return='check'/>
      <InstallExecuteSequence>
        <Custom Action='LaunchBrowser' After='InstallFinalize'>NOT Installed</Custom>
      </InstallExecuteSequence>)


}
