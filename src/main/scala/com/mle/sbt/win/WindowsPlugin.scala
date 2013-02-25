package com.mle.sbt.win

import java.nio.file.{StandardCopyOption, Files, Paths}
import sbt.Keys._
import sbt._
import com.typesafe.packager.PackagerPlugin.Windows
import com.mle.sbt.win.WindowsKeys._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.{PackagingUtil, GenericPackaging}
import com.typesafe.packager.windows


object WindowsPlugin extends Plugin {
  val windowsSettings: Seq[Setting[_]] = GenericPackaging.genericSettings ++ WixPackaging.wixSettings ++ Seq(
    msiName <<= (name in Windows, version)((n, v) => n + "-" + v + ".msi"),
    windowsPkgHome <<= (pkgHome)(_ / "windows"),
    windowsJarPath <<= (target in Windows, appJarName)((t, n) => t.toPath / n),
    exePath <<= (target in Windows, name)((t, n) => t.toPath / (n + ".exe")),
    batPath <<= (windowsPkgHome, name)((w, n) => w / (n + ".bat")),
    licenseRtf <<= (windowsPkgHome)(_ / "license.rtf"),
    appIcon <<= (windowsPkgHome)(_ / "app.ico"),
    serviceFeature := true,
    winSwExe <<= (windowsPkgHome)(_ / "winsw-1.9-bin.exe"),
    winSwConf <<= (target in Windows, winSwConfName)((t, n) => t.toPath / n),
    winSwName <<= (name)(_ + "svc"),
    winSwExeName <<= (winSwName)(_ + ".exe"),
    winSwConfName <<= (winSwName)(_ + ".xml"),
    launch4jcExe := Paths get """C:\Program Files (x86)\Launch4j\launch4jc.exe""",
    launch4jcConf <<= (target in Windows)(t => t.toPath / "launch4jconf.xml"),
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
    win <<= (windows.Keys.packageMsi in Windows, msiName, streams) map ((result, fileName, logger) => {
      val msiFile = result.toPath
      val msiRenamed = msiFile.resolveSibling(fileName)
      val packagedFile = Files.move(msiFile, msiRenamed, StandardCopyOption.REPLACE_EXISTING)
      logger.log.info("Packaged: " + packagedFile.toAbsolutePath.toString)
      packagedFile
    }) dependsOn verifySettings,
    displayName <<= (name)(n => n),
    shortcut := false,
    printMappings in Windows <<= (mappings in windows.Keys.packageMsi in Windows, streams) map ((maps, logger) => {
      val output = maps.map(kv => kv._1.getAbsolutePath + "\n" + kv._2).mkString("\n---\n")
      logger.log.info(output)
    })
  )
}