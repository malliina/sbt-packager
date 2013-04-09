# SBT Packager #

This is an SBT plugin for software packaging. It depends on [sbt-native-packager](https://raw.github.com/sbt/sbt-native-packager).

## Installation ##

    addSbtPlugin("com.github.malliina" % "sbt-packager" % "1.0.0")

## Usage ##

To get a list of tasks and settings pertaining to this plugin, run the following SBT tasks:
- helpme
- windows:helpme
- debian:helpme
- rpm:helpme

```
[myproject] $ helpme
[info] pkg-home                 Packaging home directory
[info] app-jar                  The application jar
[info] libs                     All (managed and unmanaged) libs
[info] conf-file                Configuration file
[info] version-file             Version file (written upon packaging)
[info]
[info] Three OS configurations are available: windows, debian, and rpm
[info]
[info] Try the following:
[info] windows:helpme
[info] debian:helpme
[info] rpm:helpme
[success] Total time: 0 s, completed 9.4.2013 20:05:50
[myproject] $
```

### Windows ###

```
[myproject] $ windows:helpme
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
```

### Debian ###

```
[myproject] $ debian:helpme
[info] control-dir              Directory for control files for native packaging
[info] pre-install              Preinstall script
[info] post-install             Postinstall script
[info] post-remove              Postremove script
[info] defaults-file            The defaults config file
[info] copyright-file           The copyright file
[info] changelog-file           The changelog file
[info] init-script              Init script for unix
[info] unix-home                Home dir on unix
[info] unix-lib-home            Lib dir on unix
[info] unix-script-home         Script dir on unix
[info] unix-log-home            Log dir on unix
[info] lib-mappings             Libs mapped to paths
[info] conf-mappings            Confs mapped to paths
[info] script-mappings          Scripts mapped to paths
[info] azure-upload             Packages the app and uploads it to Azure Storage
[info] azure-package            Package to upload to Azure
[info] azure-conf               Path to Azure configuration file with account_name and account_key variables.
[info] azure-container-name     The Azure storage container name
[success] Total time: 0 s, completed 9.4.2013 20:08:06
[myproject] $
```

### Rpm ###

```
[myproject] $ rpm:helpme
[info] control-dir              Directory for control files for native packaging
[info] pre-install              Preinstall script
[info] post-install             Postinstall script
[info] post-remove              Postremove script
[info] defaults-file            The defaults config file
[info] copyright-file           The copyright file
[info] changelog-file           The changelog file
[info] init-script              Init script for unix
[info] unix-home                Home dir on unix
[info] unix-lib-home            Lib dir on unix
[info] unix-script-home         Script dir on unix
[info] unix-log-home            Log dir on unix
[info] lib-mappings             Libs mapped to paths
[info] conf-mappings            Confs mapped to paths
[info] script-mappings          Scripts mapped to paths
[info] azure-upload             Packages the app and uploads it to Azure Storage
[info] azure-package            Package to upload to Azure
[info] azure-conf               Path to Azure configuration file with account_name and account_key variables.
[info] azure-container-name     The Azure storage container name
[success] Total time: 0 s, completed 9.4.2013 20:08:24
[myproject] $
```

## To do ##

- Set a better path for windows service wrapper logs

