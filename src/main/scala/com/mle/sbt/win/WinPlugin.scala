package com.mle.sbt.win

import java.nio.file.{Path, StandardCopyOption, Files, Paths}
import sbt.Keys._
import sbt._
import com.typesafe.sbt.SbtNativePackager.Windows
import com.mle.sbt.win.WinKeys._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.{PackagingUtil, GenericPlugin}
import com.typesafe.sbt.packager.windows
import java.util.UUID
import com.mle.sbt.azure.AzureKeys._
import scala.xml.Elem

object WinPlugin extends Plugin {
  val windowsMappings = mappings in windows.Keys.packageMsi

  val fileMappings: Seq[Setting[_]] = inConfig(Windows)(Seq(
    //    windowsMappings ++= msiMappings.value.map(mapping => {
    //      val (src, dest) = mapping
    //      src.toFile -> dest.toString
    //    }),
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
      val confFiles = configFiles.value.filter(_.isFile)
      val absAndRelative = confFiles.map(abs => abs -> configSrcDir.value.relativize(abs))
      absAndRelative map (ar => {
        val (abs, rel) = ar
        abs -> configDestDir.value.resolve(rel)
      })
    },
    msiMappings += appJar.value -> Paths.get(appJarName.value),
    msiMappings ++= {
      streams.value.log.info("Creating service wrapper")
      // build winsw service wrapper XML configuration file
      def toFile(xml: Elem, file: Path) {
        PackagingUtil.writerTo(file)(_.println(xml.toString()))
        streams.value.log.info("Created: " + file.toAbsolutePath)
      }
      val targetFilePath = targetPath.value
      val conf = WindowsServiceWrapper.conf(name.value, displayName.value)
      val confFile = targetFilePath / winSwConfName.value
      toFile(conf, confFile)
      val runtimeConf = WindowsServiceWrapper.netRuntimeConf
      val runtimeFile = targetFilePath / (winSwExeName.value + ".config")
      toFile(runtimeConf, runtimeFile)
      // why?
      Seq.empty[(Path, Path)]
    }
  ))

  val windowsSettings: Seq[Setting[_]] =
    GenericPlugin.genericSettings ++
      WixPackaging.wixSettings ++
      fileMappings ++
      Seq(
        appIcon := None,
        minUpgradeVersion := "0.0.0",
        uuid := UUID.randomUUID().toString,
        // wtf?
        msiMappings := Seq.empty[(Path, Path)],
        pkgHome in Windows := (pkgHome.value / "windows"),
        minJavaVersion := None,
        postInstallUrl := None,
        forceStopOnUninstall := true
      ) ++ inConfig(Windows)(GenericPlugin.confSpecificSettings ++ WixPackaging.wixSettings ++ Seq(
      help := {
        val taskList = GenericPlugin.describeWithAzure(
          windows.Keys.packageMsi,
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
          serviceFeature,
          msiMappings,
          minUpgradeVersion)
        logger.value info taskList
      },
      azurePackage := Some(win.value),
      deployFiles := msiMappings.value.map(_._2),
      configDestDir := Paths get "config",
      libDestDir := Paths get "lib",
      msiName := name.value + "-" + version.value + ".msi",
      windowsJarPath := targetPath.value / appJarName.value,
      exePath := targetPath.value / (name.value + ".exe"),
      batPath := pkgHome.value / (name.value + ".bat"),
      licenseRtf := pkgHome.value / "license.rtf",
      serviceFeature := true,
      winSwExe := pkgHome.value / "winsw-1.13-bin.exe",
      winSwConf := targetPath.value / winSwConfName.value,
      winSwName := name.value + "svc",
      winSwExeName := winSwName.value + ".exe",
      winSwConfName := winSwName.value + ".xml",
      //            winSwConfName := "conf.txt",
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
        val msiFile = windows.Keys.packageMsi.value.toPath
        val msiRenamed = msiFile.resolveSibling(msiName.value)
        val packagedFile = Files.move(msiFile, msiRenamed, StandardCopyOption.REPLACE_EXISTING)
        streams.value.log.info("Packaged: " + packagedFile.toAbsolutePath.toString)
        packagedFile
      },
      shortcut := false,
      displayName := name.value,
      mappingsPrint := {
        val maps = (mappings in windows.Keys.packageMsi in Windows).value
        val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
        streams.value.log.info(output)
      },
      serviceConf <<= (serviceFeature, winSwExe, winSwExeName, winSwConfName)((s, exe, e, cN) => if (s) Some(ServiceConf(exe, e, cN)) else None)
    ))


}