package com.mle.sbt.win

import java.nio.file.{Path, StandardCopyOption, Files, Paths}
import sbt.Keys._
import sbt._
import com.typesafe.sbt.SbtNativePackager.Windows
import com.mle.sbt.win.WindowsKeys._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.{PackagingUtil, GenericPlugin}
import com.typesafe.sbt.packager.windows
import java.util.UUID


object WindowsPlugin extends Plugin {
  val windowsSettings: Seq[Setting[_]] =
    GenericPlugin.genericSettings ++
      WixPackaging.wixSettings ++
      Seq(
        minUpgradeVersion := "0.0.0",
        uuid := UUID.randomUUID().toString,
        // wtf?
        msiMappings := Seq.empty[(Path, Path)],
        pkgHome in Windows <<= (pkgHome)(_ / "windows")
      ) ++ inConfig(Windows)(GenericPlugin.confSpecificSettings ++ Seq(
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
          launch4jcExe -> lE,
          appIcon -> aI,
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
      printMappings <<= (mappings in windows.Keys.packageMsi in Windows, streams) map ((maps, logger) => {
        val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
        logger.log.info(output)
      }),
      serviceConf <<= (serviceFeature, winSwExe, winSwExeName, winSwConfName)((s, exe, e, cN) => if (s) Some(ServiceConf(exe, e, cN)) else None)
    ))
}