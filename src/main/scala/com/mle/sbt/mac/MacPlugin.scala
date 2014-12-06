package com.mle.sbt.mac

import java.nio.file.{Files, Paths, StandardCopyOption}

import com.mle.appbundler._
import com.mle.file.StorageFile
import com.mle.sbt.GenericKeys._
import com.mle.sbt.GenericPlugin
import com.mle.sbt.mac.MacKeys._
import com.mle.sbt.unix.UnixKeys._
import com.mle.sbt.unix.UnixPlugin._
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
    defaultLaunchd := LaunchdConf(
      appIdentifier.value,
      Seq(s"/Applications/${(displayName in Mac).value}.app/Contents/MacOS/JavaAppLauncher")),
    infoPlistConf := InfoPlistConf(
      (displayName in Mac).value,
      name.value,
      appIdentifier.value,
      version.value,
      mainClass.value.getOrElse(throw new Exception("No main class specified.")),
      libs.value,
      embeddedJavaHome.value,
      jvmOptions.value,
      jvmArguments.value,
      (appIcon in Mac).value,
      hideDock = hideDock.value
    ),
    launchdConf := None,
    deleteOutOnComplete := true,
    installer := {
      val destPath = (targetPath in Mac).value
      val plist = infoPlistConf.value
      Installer(
        destPath,
        plist.displayName,
        plist.name,
        plist.version,
        organization.value,
        appIdentifier.value,
        BundleStructure(destPath / "out" / "Applications", plist.displayName),
        plist,
        launchdConf.value,
        deleteOutOnComplete = deleteOutOnComplete.value)
    },
    app := {
      val logger = streams.value.log
      logger info s"Creating app package..."
      val dName = (displayName in Mac).value
      val structure = BundleStructure(macAppTarget.value, dName)
      AppBundler.createBundle(structure, infoPlistConf.value)
      logger info s"Created ${structure.appDir}."
      structure.appDir
    },
    pkg := {
      val i = installer.value
      Files.createDirectories(i.launchdBuildPath.getParent)
      i.macPackage()
    }
  )

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
}