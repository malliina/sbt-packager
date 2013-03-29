package com.mle.sbt.unix

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager._
import java.nio.file.{Files, Paths, Path}
import linux.LinuxPackageMapping
import sbt.Keys._
import sbt._
import LinuxKeys._
import UnixKeys._
import com.mle.sbt.GenericKeys._
import scala.Some
import com.mle.sbt.FileImplicits._
import com.mle.sbt.{GenericPlugin, GenericKeys, PackagingUtil}

object LinuxPlugin extends Plugin {
  val distroSettings = GenericPlugin.confSpecificSettings ++ Seq(
    deployFiles <<= linux.Keys.linuxPackageMappings map destPaths,
    mappingsPrint <<= (linux.Keys.linuxPackageMappings, streams) map printMappings
  )
  val linuxSettings: Seq[Setting[_]] = UnixPlugin.unixSettings ++ Seq(
    /**
     * Source settings
     */
    printPaths <<= (controlDir, preInstall, postInstall, preRemove, postRemove, streams) map ((c, preI, postI, preR, postR, logger) => {
      val keys = Seq(controlDir, preInstall, postInstall, preRemove, postRemove)
      val values = Seq(c, preI, postI, preR, postR)
      val pairs = keys zip values
      PackagingUtil.logPairs(pairs, logger)
      values
    }),

    /**
     * Destination settings
     */
    // Linux directory layout
    unixHome <<= (name in Linux)(pkgName => Paths get "/opt/" + pkgName),
    unixLibDest <<= (unixHome)(_ / libDir),
    unixScriptDest <<= (unixHome)(_ / scriptDir),
    unixLogDir <<= (unixHome)(_ / logDir),
    // rpm/deb postinst control files
    controlDir <<= (pkgHome in Linux)(_ / "control"),
    preInstall <<= (controlDir)(_ / "preinstall.sh"),
    postInstall <<= (controlDir)(_ / "postinstall.sh"),
    preRemove <<= (controlDir)(_ / "preuninstall.sh"),
    postRemove <<= (controlDir)(_ / "postuninstall.sh"),
    copyrightFile <<= (pkgHome in Linux)(_ / "copyright"),
    changelogFile <<= (pkgHome in Linux)(_ / "changelog"),
    initScript <<= (pkgHome in Linux, name in Linux)((p, n) => p / (n + ".sh")),
    defaultsFile <<= (pkgHome in Linux, name in Linux)((home, n) => home / (n + ".defaults")),
    // Flat copy of libs to /lib on destination system
    libMappings <<= (libs, unixLibDest) map ((libFiles, destDir) => {
      libFiles.map(file => file -> (destDir / file.getFileName).toString)
    }),
    scriptMappings <<= (scriptFiles, scriptPath, unixScriptDest) map rebase,
    linux.Keys.packageDescription in Linux := "This is the description of the package."
  ) ++ inConfig(Linux)(distroSettings ++ Seq(
    configDestDir <<= (unixHome)(_ / confDir),
    libDestDir <<= (unixHome)(_ / libDir),
    confMappings <<= (configFiles, configSrcDir, configDestDir) map rebase,
    linux.Keys.packageSummary <<= (name)(n => "This is a summary of " + n)
  ))
  val linuxMappings: Seq[Setting[_]] = Seq(
    linux.Keys.linuxPackageMappings <++= (
      unixHome, pkgHome in Linux, name in Linux, appJar,
      scriptMappings, unixLogDir, appJarName, defaultsFile, initScript, unixLibDest
      ) map (
      (home, pkgSrc, pkgName, jarFile, scripts, uLogdir, jarName, defFile, iScript, libD) => Seq(
        fileMap(jarFile -> ((home / jarName).toString)),
        baseMaps(Seq((pkgSrc / libDir) -> libD.toString, (pkgSrc / logDir) -> uLogdir.toString), perms = "0755")
      ) ++ pkgMaps(Seq(iScript -> ("/etc/init.d/" + pkgName)) ++ scripts, perms = "0755")),
    linux.Keys.linuxPackageMappings <++= (unixHome, confMappings in Linux, name in Linux, defaultsFile,
      confFile in Linux) map ((h, confs, n, d, c) =>
      pkgMaps(c.map(cFile => Seq(cFile -> ((h / cFile.getFileName).toString)))
        .getOrElse(Seq.empty[(Path, String)]), perms = "0600", isConfig = true) ++
        pkgMaps(confs ++ Seq(d -> ("/etc/default/" + n)), perms = "0640", isConfig = true)),
    linux.Keys.linuxPackageMappings <+= libMappings map (libs => fileMaps(libs)),
    linux.Keys.linuxPackageMappings <++= pathMappings map pathMaps
  )

  val debianSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Debian)(distroSettings ++ linuxMappings) ++ Seq(
    //    debian.Keys.linuxPackageMappings <++= linux.Keys.linuxPackageMappings in Linux,
    configDestDir in Debian <<= configDestDir in Linux,
    libDestDir in Debian <<= configDestDir in Linux,
    //    debian.Keys.version := "0.1",

    debian.Keys.linuxPackageMappings in Debian <++= (pkgHome in Linux, name,
      preInstall, postInstall, preRemove, postRemove, copyrightFile, changelogFile) map (
      (pkgSrc, pkgName, preinst, postinst, prerm, postrm, cRight, changeLog) => Seq(
        // http://lintian.debian.org/tags/no-copyright-file.html
        fileMap(cRight -> ("/usr/share/doc/" + pkgName + "/copyright")),
        fileMap(changeLog -> ("/usr/share/doc/" + pkgName + "/changelog.gz"), gzipped = true) asDocs(),
        fileMaps(Seq(
          preinst -> "DEBIAN/preinst",
          postinst -> "DEBIAN/postinst",
          prerm -> "DEBIAN/prerm",
          postrm -> "DEBIAN/postrm"
        ), perms = "0755")
      ))
  )

  val rpmSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Rpm)(distroSettings ++ linuxMappings) ++ Seq(
    //    rpm.Keys.linuxPackageMappings in Rpm <++= linux.Keys.linuxPackageMappings in Linux,
    configDestDir in Rpm <<= configDestDir in Linux,
    libDestDir in Rpm <<= configDestDir in Linux,
    rpm.Keys.rpmVendor <<= (GenericKeys.manufacturer)(m => m),
    rpm.Keys.rpmLicense := Some("All rights reserved."),
    rpm.Keys.rpmPre <<= (preInstall)(fileToString),
    rpm.Keys.rpmPost <<= (postInstall)(fileToString),
    rpm.Keys.rpmPreun <<= (preRemove)(fileToString),
    rpm.Keys.rpmPostun <<= (postRemove)(fileToString)
  )

  def fileToString(file: Path) =
    if (Files exists file) {
      Some(io.Source.fromFile(file.toFile).getLines().mkString("\n"))
    } else {
      None
    }

  def pkgMap(file: (Path, String), perms: String = "0644", gzipped: Boolean = false) =
    pkgMaps(Seq(file), perms = perms, gzipped = gzipped)

  def pathMaps(maps: Seq[(Path, Path)]) = pkgMaps(maps.map(m => m._1 -> m._2.toString))

  def fileMap(mapping: (Path, String), perms: String = "0644", gzipped: Boolean = false) =
    fileMaps(Seq(mapping), perms = perms, gzipped = gzipped)

  def fileMaps(paths: Seq[(Path, String)], user: String = "root",
               group: String = "root", perms: String = "0644",
               isConfig: Boolean = false, gzipped: Boolean = false) = {
    var fileMaps = baseMaps(paths, perms, user, group)
    if (isConfig)
      fileMaps = fileMaps withConfig()
    if (gzipped)
      fileMaps = fileMaps.gzipped
    fileMaps
  }

  def baseMaps(paths: Seq[(Path, String)], perms: String,
               user: String = "root", group: String = "root") =
    LinuxPackageMapping(paths.map(pair => pair._1.toFile -> pair._2)) withPerms perms withUser user withGroup group

  def pkgMaps(paths: Seq[(Path, String)],
              user: String = "root",
              group: String = "root",
              perms: String = "0644",
              dirPerms: String = "0755",
              isConfig: Boolean = false,
              gzipped: Boolean = false) = {
    val (dirs, files) = paths partition (p => Files.isDirectory(p._1))
    val fileMappings = fileMaps(files, user, group, perms, isConfig, gzipped)
    val dirMaps = baseMaps(dirs, dirPerms)
    Seq(dirMaps, fileMappings)
  }

  def rebase(file: Path, srcBase: Path, destBase: Path) = destBase resolve (srcBase relativize file)

  def rebase(files: Seq[Path], srcBase: Path, destBase: Path): Seq[(Path, String)] =
    files map (file => file -> rebase(file, srcBase, destBase).toString)

  def rebase(files: Seq[Path], maybeSrcBase: Option[Path], destBase: Path): Seq[(Path, String)] =
    maybeSrcBase.map(srcBase => rebase(files, srcBase, destBase)).getOrElse(Seq.empty[(Path, String)])

  def printMappings(mappings: Seq[LinuxPackageMapping], logger: TaskStreams) = {
    mappings.foreach(mapping => {
      val perms = mapping.fileData.permissions
      mapping.mappings.foreach(pair => {
        val (file, dest) = pair
        val fileType = file match {
          case f if f.isFile => "file"
          case dir if file.isDirectory => "dir"
          case _ => "UNKNOWN"
        }
        logger.log.info(fileType + "(" + perms + "): " + file + ", dest: " + dest)
      })
    })
  }

  def destinations(mappings: Seq[LinuxPackageMapping]) =
    mappings.flatMap(_.mappings.map(_._2))

  def destPaths(mappings: Seq[LinuxPackageMapping]) = destinations(mappings).map(Paths.get(_))
}
