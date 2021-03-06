package com.malliina.sbt.unix

import java.nio.file.Paths

import com.malliina.appbundler.StorageFile
import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.GenericPlugin
import com.malliina.sbt.PackagingUtil._
import com.malliina.sbt.unix.UnixKeys._
import com.typesafe.sbt.SbtNativePackager.Linux
import sbt.Keys._
import sbt._

/** Using Linux configuration for Unix. Shame on me.
  */
object UnixPlugin {
  val Unix = config("unix")
  val unixSettings: Seq[Setting[_]] = GenericPlugin.genericSettings ++ Seq(
    pkgHome in Unix := pkgHome.value / "unix",
    unixHome := Paths get ("/opt/" + (name in Unix).value),
    unixLibDest := unixHome.value / libDir,
    unixScriptDest := unixHome.value / scriptDir,
    unixLogDir := unixHome.value / logDir,
    scriptPath := Some((pkgHome in Linux).value / scriptDir),
    scriptFiles := filesIn(scriptPath).value,
    // Flat copy of libs to /lib on destination system
    libMappings := libs.value.map(file => file -> (unixLibDest.value / file.getFileName)),
    scriptMappings := LinuxPlugin.rebase(scriptFiles.value, scriptPath.value, unixScriptDest.value),
    configDestDir := unixHome.value / confDir,
    libDestDir := unixHome.value / libDir,
    confMappings := LinuxPlugin.rebase(configFiles.value, configSrcDir.value, configDestDir.value)
  )
}
