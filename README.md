# SBT packager #

This is an SBT plugin for software packaging. It depends on [sbt-native-packager](https://raw.github.com/sbt/sbt-native-packager).

## Installation ##

    addSbtPlugin("com.github.malliina" % "sbt-packager" % "0.9.9")

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

## To do ##

- Set a better path for windows service wrapper logs

