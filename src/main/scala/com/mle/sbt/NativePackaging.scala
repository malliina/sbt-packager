package com.mle.sbt

import com.typesafe.packager.PackagerPlugin._
import com.typesafe.packager.{linux, debian, rpm, windows}
import sbt.Keys._
import sbt._
import java.io.File

object NativePackaging{
  val defaultPackageSettings = Seq(
    // http://lintian.debian.org/tags/maintainer-address-missing.html
    linux.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
    linux.Keys.packageSummary := "This is a summary of the package",
    linux.Keys.packageDescription := "This is the description of the package.",
    //    name := "wicket",
    debian.Keys.version := "0.1",
    // Tag takes single token only
    rpm.Keys.rpmRelease := "0.1",
    rpm.Keys.rpmVendor := "kingmichael",
    rpm.Keys.rpmLicense := Some("You have the right to remain silent"),
    windows.Keys.wixFile := new File("doesnotexist"),
    debian.Keys.linuxPackageMappings in Debian <+= (baseDirectory, name) map (
      // http://lintian.debian.org/tags/no-copyright-file.html
      (bd, pkgName) => (packageMapping((bd / "dist" / "copyright") -> ("/usr/share/doc/" + pkgName + "/copyright")) withUser "root" withPerms "0644")
      ),
    debian.Keys.linuxPackageMappings in Debian <+= (baseDirectory, name) map (
      // http://lintian.debian.org/tags/changelog-file-missing-in-native-package.html
      (bd, pkgName) => (packageMapping((bd / "dist" / "copyright") -> ("/usr/share/doc/" + pkgName + "/changelog.gz")) withUser "root" withPerms "0644" gzipped) asDocs()
      ),
    linux.Keys.linuxPackageMappings <+= (baseDirectory) map (
      (bd: File) => (packageMapping((bd / "dist" / "app.txt") -> "/opt/test/app.txt") withUser "root" withPerms "0644")
      ),
    debian.Keys.debianPackageDependencies in Debian ++= Seq("wget")
  )
}
