# SBT Packager #

This is an SBT plugin for software packaging. It depends on [sbt-native-packager](https://github.com/sbt/sbt-native-packager).

You can create app installers for Windows (.msi), Debian (.deb), RPM (.rpm) and OSX (.pkg). You can optionally specify
that your app should install itself as a service so that it starts automatically when the computer boots.

This plugin may prove useful if you wish to run Scala services on Windows or OSX. If you only use linux,
[sbt-native-packager](https://github.com/sbt/sbt-native-packager) probably works well enough.

## Installation ##

    addSbtPlugin("com.malliina" % "sbt-packager" % "2.4.1")

## Usage ##

Add the following settings to your project:

    WinPlugin.windowsSettings ++
    LinuxPlugin.linuxNativeSettings ++
    GenericPlugin.confSettings ++
    AzurePlugin.azureSettings ++
    LinuxPlugin.playSettings

Three OS configurations are available: windows, mac, debian, and rpm.

### Windows ###

    [myproject] $ windows:helpMe
    [info] package-msi              creates a new windows CAB file containing everything for the installation.
    [info] win                      Verifies settings followed by package-msi
    [info] bat-path                 Application .bat
    [info] license-rtf              Path to license RTF for windows. Shown to the user during installation.
    [info] product-guid             Product GUID required for MSI packaging. Generate with UUID.randomUUID().
    [info] upgrade-guid             Upgrade GUID required for MSI packaging. Generate with UUID.randomUUID().
    [info] uuid                     Generates a new GUID using UUID.randomUUID().
    [info] msi-name                 Name of MSI package built with task win
    [info] display-name             Display name of application
    [info] app-icon                 Path to icon (.ico) file for the application on windows
    [info] exe-path                 Application .exe path on windows during packaging
    [info] shortcut                 Whether or not to install a desktop shortcut to the main application executable
    [info] winsw-exe                Windows Service Wrapper .exe path
    [info] winsw-conf               Windows Service Wrapper .xml path on build machine
    [info] winsw-exe-name           Windows Service Wrapper executable name on target
    [info] winsw-conf-name          Windows Service Wrapper XML config file name on target
    [info] winsw-name               Windows Service Wrapper name on target
    [info] winsw-container          Winsw confs
    [info] service-feature          Whether or not to include the option to install the application as a service
    [info] msi-mappings             File mappings for MSI packaging
    [info] min-upgrade              The minimum version from which to upgrade.
    [info] azure-upload             Packages the app and uploads it to Azure Storage
    [info] azure-package            Package to upload to Azure
    [info] azure-conf               Path to Azure configuration file with account_name and account_key variables.
    [info] azure-container-name     The Azure storage container name
    [success] Total time: 0 s, completed 9.4.2013 20:07:47
    [myproject] $

To create an installation log, run the following command instead of double-clicking the packaged MSI file:

    msiexec /i "C:\app.msi" /L*V "C:\install.log"

(For more info, see http://www.advancedinstaller.com/user-guide/qa-log.html)

### Debian ###

Package with `debian:lintian`, thanks sbt-native-packager.

### Rpm ###

Package with `rpm:packageBin`, courtesy of sbt-native-packager.

### OSX ###

Add `com.malliina.sbt.mac.MacPlugin.macSettings` to your project and customize as necessary. Example:

```
import com.malliina.sbt.GenericKeys.{appIcon, displayName}
import com.malliina.sbt.mac.MacKeys.jvmOptions
import com.malliina.sbt.mac.MacPlugin.{macSettings, Mac}

def projectSettings = macSettings ++ Seq(
  jvmOptions ++= Seq("-Dhttp.port=4321"),
  appIcon in Mac := Some(Paths get "appIcon.icns"),
  displayName in Mac := "My OSX App"
)
```

To create a .pkg OSX installer of your project, run the following SBT task:

    pkg

To create a .dmg installer of your project, run:

    dmg

Available settings:

    [myproject] $ mac:helpMe
    [info] plistFile                Path to .plist file
    [info] appIdentifier            Globally unique app ID
    [info] embeddedJavaHome         Path to java home which will be embedded in the OSX .app package
    [info] jvmOptions               JVM options for OSX
    [info] jvmArguments             JVM arguments for OSX
    [info] hideDock                 If true, the app is hidden from the dock when running
    [info] infoPlistConf            The info plist conf: define to override defaults
    [info] launchdConf              The launchd configuration, if any
    [info] defaultLaunchd           The default launchd configuration, if enabled
    [info] installer                Installer conf
    [info] deleteOutOnComplete      Delete temp dir after packaging
    [info] macAppTarget             Target path to the .app package
    [info] app                      Creates a .app package
    [info] pkg                      Creates a .pkg installer
    [success] Total time: 0 s, completed 7.12.2014 15:35:59
    [myproject] $
