package com.mle.sbt.win

import java.nio.file.Paths
import sbt.Keys._
import sbt._
import com.typesafe.packager.PackagerPlugin.Windows
import com.mle.sbt.win.WindowsKeys._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.{PackagingUtil, GenericPackaging}
import com.typesafe.packager.{PackagerPlugin, windows}


object WindowsPlugin extends Plugin {
  val windowsSettings: Seq[Setting[_]] = GenericPackaging.genericSettings ++ WixPackaging.wixSettings ++ Seq(
    windowsPkgHome <<= (pkgHome)(_ / "windows"),
    windowsJarPath <<= (target in Windows, appJarName)((t, n) => t.toPath / n),
    exePath <<= (target in Windows, name)((t, n) => t.toPath / (n + ".exe")),
    batPath <<= (windowsPkgHome, name)((w, n) => w / (n + ".bat")),
    licenseRtf <<= (windowsPkgHome)(_ / "license.rtf"),
    appIcon <<= (windowsPkgHome)(_ / "app.ico"),
    winSwExe <<= (windowsPkgHome)(_ / "winsw-1.9-bin.exe"),
    winSwConf <<= (target in Windows, winSwConfName)((t, n) => t.toPath / n),
    winSwName <<= (name)(_ + "svc"),
    winSwExeName <<= (winSwName)(_ + ".exe"),
    winSwConfName <<= (winSwName)(_ + ".xml"),
    launch4jcExe := Paths get """C:\Program Files (x86)\Launch4j\launch4jc.exe""",
    launch4jcConf <<= (target in Windows)(t => t.toPath / "launch4jconf.xml"),
    verifyPaths <<= (launch4jcExe, appIcon, winSwExe, batPath, licenseRtf)
      .map((lE, aI, wE, bP, lR) => PackagingUtil.verifyPathSetting(
      launch4jcExe -> lE,
      appIcon -> aI,
      winSwExe -> wE,
      batPath -> bP,
      licenseRtf -> lR)),
    win <<= (windows.Keys.packageMsi in PackagerPlugin.Windows)(result => result) dependsOn verifyPaths
  )
}