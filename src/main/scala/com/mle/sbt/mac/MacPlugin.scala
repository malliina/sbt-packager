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
    pkgIcon := None,
    hideDock := false,
    jvmOptions := Nil,
    jvmArguments := Nil,
    embeddedJavaHome := InfoPlistConf.DEFAULT_JAVA,
    extraDmgFiles := Nil,
    defaultLaunchd := LaunchdConf(
      appIdentifier.value,
      Seq(LaunchdConf.executable((displayName in Mac).value))),
    infoPlistConf := {
      val libraryJars = libs.value
      val jars =
        if(exportJars.value) libraryJars
        else libraryJars :+ (packageBin in Compile).value.toPath
      InfoPlistConf(
        (displayName in Mac).value,
        name.value,
        appIdentifier.value,
        version.value,
        mainClass.value.getOrElse(throw new Exception("No main class specified.")),
        jars,
        embeddedJavaHome.value,
        jvmOptions.value,
        jvmArguments.value,
        (appIcon in Mac).value,
        hideDock = hideDock.value
      )
    },
    launchdConf := None,
    deleteOutOnComplete := true,
    installer := {
      Installer(
        (targetPath in Mac).value,
        infoPlistConf.value,
        launchdConf.value,
        additionalDmgFiles = extraDmgFiles.value,
        iconFile = pkgIcon.value,
        deleteOutOnComplete = deleteOutOnComplete.value)
    },
    app := {
      val logger = streams.value.log
      logger info s"Creating app package..."
      val plist = infoPlistConf.value
      val appDir = AppBundler.createBundle(plist, macAppTarget.value)
      logger info s"Created $appDir."
      appDir
    },
    pkg := {
      val i = installer.value
      i.macPackage()
    },
    dmg := {
      val i = installer.value
      i.dmgPackage()
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
    }),
    helpMe := {
      val taskList = GenericPlugin.describe(
        plistFile,
        appIdentifier,
        embeddedJavaHome,
        jvmOptions,
        jvmArguments,
        hideDock,
        infoPlistConf,
        launchdConf,
        defaultLaunchd,
        installer,
        deleteOutOnComplete,
        macAppTarget,
        app,
        pkg
      )
      logger.value info taskList
    }
  ) ++ GenericPlugin.confSpecificSettings)
}