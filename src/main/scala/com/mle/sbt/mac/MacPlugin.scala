package com.mle.sbt.mac

import java.nio.file.{Files, StandardCopyOption}

import com.mle.appbundler.AppBundler.BundleStructure
import com.mle.appbundler.{AppBundler, InfoPlistConf}
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.GenericPlugin
import com.mle.sbt.mac.MacKeys._
import com.mle.sbt.unix.LinuxKeys._
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
    appIdentifier := s"${organization.value}.${name.value}",
    hideDock := false,
    app := {
      val mClass = mainClass.value.getOrElse(throw new Exception("No main class specified."))
      val dName = (displayName in Mac).value
      val structure = BundleStructure((targetPath in Mac).value / "OSXapp", dName)
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
      structure.appDir
    }
  )

  protected def macConfigSettings: Seq[Setting[_]] = inConfig(Mac)(Seq(
    pkgHome := pkgHome.value / "mac",
    target := target.value / "mac",
    initScript := pkgHome.value / (name.value + ".sh"),
    plistFile := pkgHome.value / "launchd.plist",
    pathMappings := confMappings.value ++ scriptMappings.value ++ libMappings.value,
    deployFiles := pathMappings.value.map(_._2),
    prepareFiles := pathMappings.value.map(p => {
      val (src, dest) = p
      //      val destString = dest.toAbsolutePath.toString
      //      val localDest = targetPath.value / (if (destString startsWith "/") destString.tail else destString)
      Option(dest.getParent).foreach(dir => Files.createDirectories(dir))
      Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
    })
  ) ++ GenericPlugin.confSpecificSettings)
}