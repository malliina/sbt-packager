package com.mle.sbt.unix

import com.typesafe.packager.PackagerPlugin._
import com.typesafe.packager._
import java.nio.file.{Paths, Path}
import linux.LinuxPackageMapping
import sbt.Keys._
import sbt._
import LinuxKeys._
import UnixKeys._
import com.mle.sbt.GenericKeys._
import scala.Some
import com.mle.sbt.win.WixPackaging
import com.mle.sbt.FileImplicits._

object LinuxPackaging extends Plugin {
  val linuxSettings: Seq[Setting[_]] = UnixPackaging.unixSettings ++ Seq(
    /**
     * Source settings
     */
    /**
     * Destination settings
     */
    // Linux directory layout
    unixHome <<= (name)(pkgName => Paths get "/opt/" + pkgName),
    unixLibDest <<= (unixHome)(_ / libDir),
    unixConfDest <<= (unixHome)(_ / confDir),
    unixScriptDest <<= (unixHome)(_ / scriptDir),
    unixLogDir <<= (unixHome)(_ / "logs"),
    // rpm/deb postinst control files
    controlDir <<= (unixPkgHome)(_ / "control"),
    preInstall <<= (controlDir)(_ / "preinstall.sh"),
    postInstall <<= (controlDir)(_ / "postinstall.sh"),
    preRemove <<= (controlDir)(_ / "preuninstall.sh"),
    postRemove <<= (controlDir)(_ / "postuninstall.sh"),
    // Flat copy of libs to /lib on destination system
    libMappings <<= (libs, unixLibDest) map ((libFiles, destDir) => {
      libFiles.map(file => file -> (destDir / file.getFileName).toString)
    }),
    confMappings <<= (configFiles, configPath, unixConfDest) map rebase,
    scriptMappings <<= (scriptFiles, scriptPath, unixScriptDest) map rebase,
    // http://lintian.debian.org/tags/maintainer-address-missing.html
    linux.Keys.maintainer := "Michael Skogberg <malliina123@gmail.com>",
    linux.Keys.packageSummary := "This is a summary of the package",
    linux.Keys.packageDescription := "This is the description of the package.",
    //    name := "wicket",
    linux.Keys.linuxPackageMappings in Linux <++= (
      unixHome, unixPkgHome, name, appJar, libMappings, confMappings,
      scriptMappings, unixLogDir, appJarName
      ) map (
      (home, pkgSrc, pkgName, jarFile, libs, confs, scripts, logDir, jarName) => Seq(
        pkgMaps(Seq((pkgSrc / (pkgName + ".sh")) -> ("/etc/init.d/" + pkgName)) ++ scripts, perms = "0755"),
        pkgMaps(libs),
        pkgMaps(confs ++ Seq((pkgSrc / (pkgName + ".defaults")) -> ("/etc/default/" + pkgName)), isConfig = true),
        pkgMap((pkgSrc / "logs") -> logDir.toString, perms = "0755"),
        pkgMap(jarFile -> ((home / jarName).toString))
      ))
  )
  val debianSettings: Seq[Setting[_]] = linuxSettings ++ Seq(
    debian.Keys.linuxPackageMappings in Debian <++= linux.Keys.linuxPackageMappings in Linux,
    debian.Keys.version := "0.1",
    debian.Keys.linuxPackageMappings in Debian <++= (unixPkgHome, name,
      preInstall, postInstall, preRemove, postRemove) map (
      (pkgSrc, pkgName, preinst, postinst, prerm, postrm) => Seq(
        // http://lintian.debian.org/tags/no-copyright-file.html
        pkgMap((pkgSrc / "copyright") -> ("/usr/share/doc/" + pkgName + "/copyright")),
        pkgMap((pkgSrc / "changelog") -> ("/usr/share/doc/" + pkgName + "/changelog.gz"), gzipped = true) asDocs(),
        pkgMaps(Seq(
          preinst -> "DEBIAN/preinst",
          postinst -> "DEBIAN/postinst",
          prerm -> "DEBIAN/prerm",
          postrm -> "DEBIAN/postrm"
        ), perms = "0755")
      ))
    ,
    debFiles <<= (debian.Keys.linuxPackageMappings in Debian, name) map ((mappings, pkgName) => {
      printMappings(mappings)
    })
//    debian.Keys.debianPackageDependencies in Debian ++= Seq("wget")

  )
  val rpmSettings: Seq[Setting[_]] = linuxSettings ++ Seq(
    rpm.Keys.linuxPackageMappings in Rpm <++= linux.Keys.linuxPackageMappings in Linux,
    rpm.Keys.rpmRelease := "0.1",
    rpm.Keys.rpmVendor := "kingmichael",
    rpm.Keys.rpmLicense := Some("You have the right to remain silent"),
    rpm.Keys.rpmPreInstall <<= (preInstall)(Some(_)),
    rpm.Keys.rpmPostInstall <<= (postInstall)(Some(_)),
    rpm.Keys.rpmPreRemove <<= (preRemove)(Some(_)),
    rpm.Keys.rpmPostRemove <<= (postRemove)(Some(_))    ,
    rpmFiles <<= (rpm.Keys.linuxPackageMappings in Rpm, name) map ((mappings, pkgName) => {
      printMappings(mappings)
    })
  )
  val defaultNativeProject: Seq[Setting[_]] = linuxSettings ++ debianSettings ++ rpmSettings ++ WixPackaging.wixSettings

  def pkgMap(file: (Path, String), perms: String = "0644", gzipped: Boolean = false) =
    pkgMaps(Seq(file), perms = perms, gzipped = gzipped)

  def pkgMaps(files: Seq[(Path, String)],
              user: String = "root",
              group: String = "root",
              perms: String = "0644",
              isConfig: Boolean = false,
              gzipped: Boolean = false) = {
    var mapping = LinuxPackageMapping(files.map(pair => pair._1.toFile -> pair._2)) withUser user withGroup group withPerms perms
    if (isConfig)
      mapping = mapping withConfig()
    if (gzipped)
      mapping = mapping.gzipped
    mapping
  }

  def pkgMapping(files: (Path, String)*) = {
    packageMapping(files.map(pair => pair._1.toFile -> pair._2): _*)
    packageMapping()
  }

  def printMapping(mapping: LinuxPackageMapping) {
    mapping.mappings.foreach(ping => {
      val (file, dest) = ping
      println("file: " + file + ", dest: " + dest)
    })
  }

  def rebase(file: Path, srcBase: Path, destBase: Path) = destBase resolve (srcBase relativize file)

  def rebase(files: Seq[Path], srcBase: Path, destBase: Path): Seq[(Path, String)] =
    files map (file => file -> rebase(file, srcBase, destBase).toString)

  def rebase(files: Seq[Path], maybeSrcBase: Option[Path], destBase: Path): Seq[(Path, String)] =
    maybeSrcBase.map(srcBase => rebase(files, srcBase, destBase)).getOrElse(Seq.empty[(Path, String)])
  def printMappings(mappings: Seq[LinuxPackageMapping]) = {
    mappings.foreach(mapping => {
      mapping.mappings.foreach(pair => {
        val (file, dest) = pair
        val fileType = if (file.isFile) "file"
        else {
          if (file.isDirectory) "dir" else "UNKNOWN"
        }
        println(fileType + ": " + file + ", dest: " + dest)
      })
    })
    val ret = mappings.flatMap(_.mappings.map(_._2))
    ret foreach println
    ret
  }
}
