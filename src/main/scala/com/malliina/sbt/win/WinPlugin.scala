package com.malliina.sbt.win

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.util.UUID

import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.win.WinKeys._
import com.malliina.sbt.{GenericPlugin, PackagingUtil}
import com.typesafe.sbt.SbtNativePackager.{Universal, Windows}
import sbt.Keys._
import sbt._

object WinPlugin {
  val windowsKeys = com.typesafe.sbt.packager.Keys
  val windowsMappings = mappings in packageBin in Windows

  val fileMappings: Seq[Setting[_]] = inConfig(Windows)(
    Seq(
      windowsMappings ++= msiMappings.value.map {
        case (src, dest) => src.toFile -> dest.toString
      },
      // Includes universal mappings except docs
      msiMappings in Windows := (mappings in Universal).value
        .filter { case (_, dest) => !dest.startsWith("share") }
        .map { case (src, dest) => (src.toPath, Paths.get(dest)) },
      msiMappings ++= {
        val src = (packageBin in Compile).value.toPath
        if (exportJars.value) {
          Nil
        } else {
          val dest = libDestDir.value.resolve(src.getFileName)
          Seq(src -> dest)
        }
      }
    )
  )

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
    ) ++ inConfig(Windows)(
    fileMappings ++
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
          minUpgradeVersion
        )
        logger.value info taskList
      },
      deployFiles := msiMappings.value.map(_._2),
      binDestDir := Paths get "bin",
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
      winSwStartExecutable := s"%BASE%\\bin\\${name.value}.bat",
      winSwStopExecutable := s"%BASE%\\bin\\${name.value}.bat",
      winSwStartArgument := "start",
      winSwStopArgument := "stop",
      winSwLogPath := s"%SystemDrive%\\ProgramData\\${displayName.value}\\logs",
      launch4jcExe := Paths get """C:\Program Files (x86)\Launch4j\launch4jc.exe""",
      launch4jcConf := targetPath.value / "launch4jconf.xml",
      verifySettings := {
//      if (mainClass.value.isEmpty)
//        throw new Exception("No mainClass specified; cannot create .exe")
//      PackagingUtil.verifyPathSetting(
//        winSwExe -> winSwExe.value,
//        batPath -> batPath.value,
//        licenseRtf -> licenseRtf.value)
      },
      printPaths := {
        val keys = Seq(launch4jcExe, winSwExe, batPath, licenseRtf)
        val values = Seq(launch4jcExe.value, winSwExe.value, batPath.value, licenseRtf.value)
        val pairs = keys zip values
        PackagingUtil.logPairs(pairs, streams.value)
        values
      },
      stopParentProcessFirst := true,
      preparePackaging := {
        val log = streams.value.log
        val conf =
          if (useTerminateProcess.value) {
            ShortWinswConf(
              name.value,
              displayName.value,
              winSwStartExecutable.value,
              winSwLogPath.value,
              stopParentProcessFirst.value
            )
          } else {
            FullWinswConf(
              name.value,
              displayName.value,
              winSwStartExecutable.value,
              winSwStartArgument.value,
              winSwStopExecutable.value,
              winSwStopArgument.value,
              winSwLogPath.value,
              stopParentProcessFirst.value
            )
          }
        serviceImplementation.value
          .map { impl =>
            impl.prepare(conf, winSwXmlTargetPath.value, runtimeConfTargetPath.value, log)
          }
          .getOrElse {
            log.info("Not packaging as a service.")
            Nil
          }
      },
      win := {
        val msiFile = (packageBin in Windows).value.toPath
        val msiRenamed = msiFile.resolveSibling(msiName.value)
        val packagedFile = Files.move(msiFile, msiRenamed, StandardCopyOption.REPLACE_EXISTING)
        streams.value.log.info("Packaged: " + packagedFile.toAbsolutePath.toString)
        packagedFile
      },
      win := win.dependsOn(preparePackaging).value,
      shortcut := false,
      mappingsPrint := {
        val maps = (mappings in packageBin in Windows).value
        val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
        streams.value.log.info(output)
      },
      serviceImplementation := Some(WinKeys.Winsw),
      serviceConf := {
        serviceImplementation.value.map { s =>
          ServiceConf(
            winSwExe.value,
            winSwExeName.value,
            runtimeConfTargetPath.value,
            winSwXmlTargetPath.value
          )
        }
      }
    )
  )
}
