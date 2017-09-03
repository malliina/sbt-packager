package com.malliina.sbt.win

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util.UUID

import com.malliina.file.StorageFile
import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.win.WinKeys._
import com.malliina.sbt.{GenericPlugin, PackagingUtil}
import com.typesafe.sbt.SbtNativePackager.Windows
import sbt.Keys._
import sbt._

object WinPlugin extends Plugin {
  val windowsKeys = com.typesafe.sbt.packager.Keys
  val windowsMappings = mappings in packageBin in Windows

  val fileMappings: Seq[Setting[_]] = inConfig(Windows)(Seq(
    windowsMappings ++= msiMappings.value.map {
      case (src, dest) => src.toFile -> dest.toString
    },
    msiMappings ++= {
      val batFilePath = batPath.value
      def servicePath(suffix: String) = targetPath.value / (name.value + suffix)
      val startService = servicePath("-start.bat")
      val stopService = servicePath("-stop.bat")
      def write(path: Path, batCommand: String) = PackagingUtil.writerTo(path)(_.println(batFilePath.getFileName.toString + batCommand))
      write(startService, " start")
      write(stopService, " stop")
      val confMap = confFile.value.map(p => Seq(p -> p.getFileName)).getOrElse(Seq.empty[(Path, Path)])
      confMap ++ Seq(
        batFilePath -> batFilePath.getFileName,
        startService -> startService.getFileName,
        stopService -> stopService.getFileName
      )
    },
    msiMappings ++= libs.value.map(libPath => libPath -> (libDestDir.value / libPath.getFileName)),
    msiMappings ++= {
      val confFiles = configFiles.value.filter(_.toFile.isFile)
      val absAndRelative = confFiles.map(abs => abs -> configSrcDir.value.relativize(abs))
      absAndRelative map (ar => {
        val (abs, rel) = ar
        abs -> configDestDir.value.resolve(rel)
      })
    },
    msiMappings ++= {
      val log = streams.value.log
      // TODO fix this
      serviceImplementation.value.fold(log.info("No service implementation."))(_.prepare(
        streams.value.log,
        targetPath.value,
        name.value,
        displayName.value,
        winSwConfName.value,
        runtimeConfTargetPath.value))
      // the prepare method already creates the files and puts them into the target directory, so it would seem
      // we don't need to do the mapping here, but this is extremely confusing
      Seq.empty[(Path, Path)]
    },
    msiMappings ++= {
      if (exportJars.value) {
        Nil
      } else {
        val src = (packageBin in Compile).value.toPath
        val dest = libDestDir.value / src.getFileName
        Seq(src -> dest)
      }
    }
  ))

  val windowsSettings: Seq[Setting[_]] = GenericPlugin.genericSettings ++
    Seq(
      appIcon := None,
      minUpgradeVersion := "0.0.0",
      uuid := UUID.randomUUID().toString,
      // wtf?
      msiMappings := Seq.empty[(Path, Path)],
      pkgHome in Windows := (pkgHome.value / "windows"),
      minJavaVersion := None,
      postInstallUrl := None,
      interactiveInstallation := false,
      forceStopOnUninstall := interactiveInstallation.value,
      productGuid := "*",
      msi := (win in Windows).value
    ) ++ inConfig(Windows)(fileMappings ++
      GenericPlugin.confSpecificSettings ++
      WixPackaging.wixSettings ++ Seq(
      helpMe := {
        val taskList = GenericPlugin.describeWithAzure(
          packageBin in Windows,
          win,
          batPath,
          licenseRtf,
          productGuid,
          upgradeGuid,
          uuid,
          msiName,
          displayName,
          appIcon,
          exePath,
          shortcut,
          winSwExe,
          winSwConf,
          winSwExeName,
          winSwConfName,
          winSwName,
          serviceConf,
          msiMappings,
          minUpgradeVersion)
        logger.value info taskList
      },
      deployFiles := msiMappings.value.map(_._2),
      configDestDir := Paths get "config",
      libDestDir := Paths get "lib",
      msiName := name.value + "-" + version.value + ".msi",
      windowsJarPath := targetPath.value / appJarName.value,
      exePath := targetPath.value / (name.value + ".exe"),
      batPath := (pkgHome in Windows).value / (name.value + ".bat"),
      licenseRtf := (pkgHome in Windows).value / "license.rtf",
      winSwExe := (pkgHome in Windows).value / "WinSW.NET2.exe",
      winSwConf := targetPath.value / winSwConfName.value,
      winSwName := name.value + "svc",
      winSwExeName := winSwName.value + ".exe",
      winSwConfName := winSwName.value + ".xml",
      runtimeConfTargetPath := targetPath.value / (winSwExeName.value + ".config"),
      winSwXmlTargetPath := targetPath.value / winSwConfName.value,
      launch4jcExe := Paths get """C:\Program Files (x86)\Launch4j\launch4jc.exe""",
      launch4jcConf := targetPath.value / "launch4jconf.xml",
      verifySettings := {
        if (mainClass.value.isEmpty)
          throw new Exception("No mainClass specified; cannot create .exe")
        PackagingUtil.verifyPathSetting(
          winSwExe -> winSwExe.value,
          batPath -> batPath.value,
          licenseRtf -> licenseRtf.value)
      },
      printPaths := {
        val keys = Seq(launch4jcExe, winSwExe, batPath, licenseRtf)
        val values = Seq(launch4jcExe.value, winSwExe.value, batPath.value, licenseRtf.value)
        val pairs = keys zip values
        PackagingUtil.logPairs(pairs, streams.value)
        values
      },
      win := {
        val msiFile = (packageBin in Windows).value.toPath
        val msiRenamed = msiFile.resolveSibling(msiName.value)
        val packagedFile = Files.move(msiFile, msiRenamed, StandardCopyOption.REPLACE_EXISTING)
        streams.value.log.info("Packaged: " + packagedFile.toAbsolutePath.toString)
        packagedFile
      },
      shortcut := false,
      mappingsPrint := {
        val maps = (mappings in packageBin in Windows).value
        val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
        streams.value.log.info(output)
      },
      serviceImplementation := Some(WinKeys.Winsw),
      serviceConf := {
        serviceImplementation.value.map { s =>
          ServiceConf(winSwExe.value, winSwExeName.value, runtimeConfTargetPath.value, winSwXmlTargetPath.value)
        }
      }
    ))
}
