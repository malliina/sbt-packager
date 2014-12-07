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
      Seq(LaunchdConf.executable((displayName in Mac).value))),
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
      Installer(
        (targetPath in Mac).value,
        infoPlistConf.value,
        launchdConf.value,
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