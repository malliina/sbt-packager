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


object WinPlugin extends Plugin {
  val windowsMappings = mappings in windows.Keys.packageMsi

  val fileMappings: Seq[Setting[_]] = inConfig(Windows)(Seq(

    windowsMappings <++= (msiMappings) map ((msiMaps: Seq[(Path, Path)]) => msiMaps.map(mapping => {
      val (src, dest) = mapping
      src.toFile -> dest.toString
    })),
    //    msiMappings <++= pathMappings,
    msiMappings <++= (batPath, confFile, name, targetPath) map ((b, c, n, t) => {
      val startService = t / (n + "-start.bat")
      val stopService = t / (n + "-stop.bat")
      PackagingUtil.writerTo(startService)(_.println(n + ".bat start"))
      PackagingUtil.writerTo(stopService)(_.println(n + ".bat stop"))
      val confMap = c.map(p => Seq(p -> p.getFileName)).getOrElse(Seq.empty[(Path, Path)])
      confMap ++ Seq(
        b -> b.getFileName,
        startService -> startService.getFileName,
        stopService -> stopService.getFileName
      )
    }),
    msiMappings <++= (libs, libDestDir) map ((libz, libDest) => {
      libz.map(libPath => (libPath -> (libDest / libPath.getFileName)))
    }),
    msiMappings <++= (configFiles, configSrcDir, configDestDir) map ((confs, baseDir, confDest) => {
      val confFiles = confs.filter(_.isFile)
      val absAndRelative = confFiles.map(abs => abs -> baseDir.relativize(abs))
      absAndRelative map (ar => {
        val (abs, rel) = ar
        abs -> confDest.resolve(rel)
      })
      //      PackagingUtil.relativize(confs.filter(_.isFile), Option(baseDir.getParent).getOrElse(baseDir))
    }),
    msiMappings <+= (appJar, appJarName) map (
      (jar, jarName) => jar -> Paths.get(jarName)),
    //    msiMappings <++= (appJar, appJarName, name, exePath, mainClass, launch4jcExe, appIcon, targetPath) map (
    //      (bin, jarName, appName, exeP, m, l, i, t) => {
    //        val mClass = m.getOrElse(throw new Exception("No mainClass specified; cannot create .exe"))
    //        val exeFile = Launch4jWrapper.exeWrapper(l, bin, mClass, jarName, t / "launch4jconf.xml", exeP, i)
    //        exeFile -> exeP
    //        Seq.empty[(Path, Path)]
    //      }),
    msiMappings <++= (name, targetPath, winSwExe, winSwExeName, winSwConfName, displayName, streams) map (
      (n, t, w, wN, c, d, l) => {
        l.log.info("Creating service wrapper")
        // build winsw service wrapper XML configuration file
        val conf = WindowsServiceWrapper.conf(n, d)
        val confFile = t / c
        PackagingUtil.writerTo(confFile)(_.println(conf.toString()))
        l.log.info("Created: " + confFile.toAbsolutePath)
        //        Seq(confFile.toFile -> c,w.toFile -> wN)
        Seq.empty[(Path, Path)]
      })))

  val windowsSettings: Seq[Setting[_]] =
    GenericPlugin.genericSettings ++
      WixPackaging.wixSettings ++
      fileMappings ++
      Seq(
        minUpgradeVersion := "0.0.0",
        uuid := UUID.randomUUID().toString,
        // wtf?
        msiMappings := Seq.empty[(Path, Path)],
        pkgHome in Windows <<= (pkgHome)(_ / "windows"),
        minJavaVersion := Some(7)
      ) ++ inConfig(Windows)(GenericPlugin.confSpecificSettings ++ Seq(
      help <<= (logger) map (log => {
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
        log info taskList
      }),
      azurePackage <<= win map (msi => Some(msi)),
      msiMappings <++= pathMappings,
      deployFiles <<= msiMappings map (msis => msis.map(_._2)),
      configDestDir := Paths get "config",
      libDestDir := Paths get "lib",
      msiName <<= (name, version)((n, v) => n + "-" + v + ".msi"),
      windowsJarPath <<= (targetPath, appJarName)((t, n) => t / n),
      exePath <<= (targetPath, name)((t, n) => t / (n + ".exe")),
      batPath <<= (pkgHome, name)((w, n) => w / (n + ".bat")),
      licenseRtf <<= (pkgHome)(_ / "license.rtf"),
      appIcon <<= (pkgHome)(_ / "app.ico"),
      serviceFeature := true,
      winSwExe <<= (pkgHome)(_ / "winsw-1.9-bin.exe"),
      winSwConf <<= (targetPath, winSwConfName)((t, n) => t / n),
      winSwName <<= (name)(_ + "svc"),
      winSwExeName <<= (winSwName)(_ + ".exe"),
      winSwConfName <<= (winSwName)(_ + ".xml"),
      launch4jcExe := Paths get """C:\Program Files (x86)\Launch4j\launch4jc.exe""",
      launch4jcConf <<= (targetPath)(t => t / "launch4jconf.xml"),
      verifySettings <<= (launch4jcExe, appIcon, winSwExe, batPath, licenseRtf, mainClass)
        .map((lE, aI, wE, bP, lR, mC) => {
        if (mC.isEmpty)
          throw new Exception("No mainClass specified; cannot create .exe")
        PackagingUtil.verifyPathSetting(
          //          launch4jcExe -> lE,
          //          appIcon -> aI,
          winSwExe -> wE,
          batPath -> bP,
          licenseRtf -> lR)
      }),
      printPaths <<= (launch4jcExe, appIcon, winSwExe, batPath, licenseRtf, streams)
        .map((lE, aI, wE, bP, lR, logger) => {
        val keys = Seq(launch4jcExe, appIcon, winSwExe, batPath, licenseRtf)
        val values = Seq(lE, aI, wE, bP, lR)
        val pairs = keys zip values
        PackagingUtil.logPairs(pairs, logger)
        values
      }),
      win <<= (windows.Keys.packageMsi, msiName, streams) map ((result, fileName, logger) => {
        val msiFile = result.toPath
        val msiRenamed = msiFile.resolveSibling(fileName)
        val packagedFile = Files.move(msiFile, msiRenamed, StandardCopyOption.REPLACE_EXISTING)
        logger.log.info("Packaged: " + packagedFile.toAbsolutePath.toString)
        packagedFile
      }) dependsOn verifySettings,
      shortcut := false,
      displayName <<= (name)(n => n),
      mappingsPrint <<= (mappings in windows.Keys.packageMsi in Windows, streams) map ((maps, logger) => {
        val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
        logger.log.info(output)
      }),
      serviceConf <<= (serviceFeature, winSwExe, winSwExeName, winSwConfName)((s, exe, e, cN) => if (s) Some(ServiceConf(exe, e, cN)) else None)
    ))


}