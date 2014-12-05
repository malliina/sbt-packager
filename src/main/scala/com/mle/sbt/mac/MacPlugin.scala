package com.mle.sbt.mac

import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import com.mle.appbundler.AppBundler.BundleStructure
import com.mle.appbundler.{AppBundler, InfoPlistConf}
import com.mle.file.FileUtilities
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.mac.MacKeys._
import com.mle.sbt.unix.UnixKeys._
import com.mle.sbt.unix.UnixPlugin._
import com.mle.sbt.{GenericKeys, GenericPlugin}
import sbt.Keys._
import sbt.{Plugin, _}

/**
 * @author mle
 */
object MacPlugin extends Plugin {
  val Mac = config("mac") extend Unix

  def macSettings: Seq[Setting[_]] = unixSettings ++ macOnlySettings ++ macConfigSettings

  protected def macOnlySettings: Seq[Setting[_]] = Seq(
    pkgHome in Mac := pkgHome.value / "mac",
    target in Mac := target.value / "mac",
    macAppTarget := (targetPath in Mac).value / "OSXapp",
    appIdentifier := s"${organization.value}.${name.value}",
    hideDock := false,
    jvmOptions := Nil,
    jvmArguments := Nil,
    embeddedJavaHome := Paths get "/usr/libexec/java_home",
    infoPlistConf := None,
    app := {
      val logger = streams.value.log
      logger info s"Creating app package..."
      val mClass = mainClass.value.getOrElse(throw new Exception("No main class specified."))
      val dName = (displayName in Mac).value
      val structure = BundleStructure(macAppTarget.value, dName)
      val plist = infoPlistConf.value getOrElse InfoPlistConf(
        dName,
        name.value,
        appIdentifier.value,
        version.value,
        mClass,
        libs.value,
        embeddedJavaHome.value,
        jvmOptions.value,
        jvmArguments.value,
        (appIcon in Mac).value,
        hideDock = hideDock.value
      )
      AppBundler.createBundle(structure, plist)
      logger info s"Created ${structure.appDir}."
      structure.appDir
    }
  )

  def macPackaging = Seq(
    macPkgRoot := (targetPath in Mac).value / "out",
    macAppTarget := macPkgRoot.value / "Applications",
    macAppDir := macAppTarget.value / s"${displayName.value}.app",
    macContentsDir := macAppDir.value / "Contents",
    macDistribution := (targetPath in Mac).value / "Distribution.xml",
    macResources := (targetPath in Mac).value / "Resources",
    macScripts := (targetPath in Mac).value / "Scripts",
    macPkgDir := (targetPath in Mac).value / "Pkg",
    macPackagePath := ((targetPath in Mac).value / s"${name.value}-${version.value}.pkg"),
    macPkgBuild := Seq(
      "/usr/bin/pkgbuild",
      "--root",
      macPkgRoot.value.toString,
      "--identifier",
      appIdentifier.value,
      "--version",
      version.value,
      "--scripts",
      macScripts.value.toString,
      "--ownership",
      "recommended",
      (macPkgDir.value / s"${name.value}.pkg").toString),
    macProductBuild := Seq(
      "/usr/bin/productbuild",
      "--distribution",
      macDistribution.value.toString,
      "--resources",
      macResources.value.toString,
      "--version",
      version.value,
      "--package-path",
      macPkgDir.value.toString,
      macPackagePath.value.toString),
    macPackage := {
      Files.createDirectories(macPkgRoot.value)
      val pkgHome = (GenericKeys.pkgHome in Mac).value
      copy(pkgHome / "Distribution.xml", (targetPath in Mac).value)
      copy(pkgHome / "welcome.html", macResources.value)
      copy(pkgHome / "license.html", macResources.value)
      copy(pkgHome / "conclusion.html", macResources.value)
      Files.createDirectories(macScripts.value)
      copyExecutable(pkgHome / "preinstall", macScripts.value)
      copyExecutable(pkgHome / "postinstall", macScripts.value)
      copy(pkgHome / s"${organization.value}.plist", macPkgRoot.value / "Library" / "LaunchDaemons")
      app.value
      val logger = streams.value
      //      val bundle = macAppDir.value
      //      val cmd = Seq("/usr/bin/SetFile", "-a", "B", bundle.toString)
      //      ExeUtils.execute(cmd, streams.value)
      Files.createDirectories(macPkgDir.value)
      ExeUtils.execute(macPkgBuild.value, logger)
      ExeUtils.execute(macProductBuild.value, logger)
      val outFile = macPackagePath.value

      /**
       * If the out directory used to build the .pkg is not deleted, the app will fail to install properly on the
       * development machine. I don't know why, I suspect I'm doing something wrong, but deleting the directory is a
       * workaround.
       */
      AppBundler.delete(macPkgRoot.value)
      logger.log info s"Created $outFile"
      outFile
    }
  )

  def writePostInstall(identifier: String, pkgRoot: Path, buildDest: Path) = {
    scriptify(identifier, pkgRoot, buildDest)(p => {
      s"""#!/bin/sh
        |set -s
        |/bin/launchctl load "$p"
      """
    })
  }

  def writePreInstall(identifier: String, pkgRoot: Path, buildDest: Path) =
    scriptify(identifier, pkgRoot, buildDest)(p => {
      s"""#!/bin/sh
        |set -e
        |if /bin/launchctl list "$identifier" &> /dev/null; then
        |    /bin/launchctl unload "$p"
        |fi"""
    })

  def scriptify(identifier: String, pkgRoot: Path, buildDest: Path)(f: Path => String) = {
    val p = pkgRoot / "Library" / "LaunchDaemons" / s"$identifier.plist"
    FileUtilities.writerTo(buildDest)(w => w.println(f(p).stripMargin))
  }

  protected def macConfigSettings: Seq[Setting[_]] = inConfig(Mac)(Seq(
    plistFile := pkgHome.value / "launchd.plist",
    pathMappings := confMappings.value ++ scriptMappings.value ++ libMappings.value,
    deployFiles := pathMappings.value.map(_._2),
    prepareFiles := pathMappings.value.map(p => {
      val (src, dest) = p
      Option(dest.getParent).foreach(dir => Files.createDirectories(dir))
      Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
    })
  ) ++ GenericPlugin.confSpecificSettings)

  def copy(src: Path, destDir: Path): Path = {
    Files.createDirectories(destDir)
    Files.copy(src, destDir / src.getFileName, StandardCopyOption.REPLACE_EXISTING)
  }

  def copyExecutable(src: Path, destDir: Path): Path = {
    val result = copy(src, destDir)
    result.toFile.setExecutable(true, false)
    result
  }
}