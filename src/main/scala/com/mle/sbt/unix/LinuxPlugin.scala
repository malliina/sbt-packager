package com.mle.sbt.unix

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager._
import java.nio.file.{Files, Paths, Path}
import sbt.Keys._
import sbt._
import com.mle.sbt.unix.LinuxKeys._
import UnixKeys._
import com.mle.sbt.GenericKeys._
import com.mle.sbt.{GenericPlugin, GenericKeys, PackagingUtil}
import com.mle.sbt.azure.{AzurePlugin, AzureKeys}
import scala.Some
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import com.mle.sbt.FileImplicits._

object LinuxPlugin extends Plugin {
  val distroSettings = GenericPlugin.confSpecificSettings ++ Seq(
    deployFiles := destPaths(linux.Keys.linuxPackageMappings.value),
    mappingsPrint := printMappings(linux.Keys.linuxPackageMappings.value, streams.value),
    help := {
      val msg = GenericPlugin.describeWithAzure(
        controlDir,
        preInstall,
        postInstall,
        preRemove,
        postRemove,
        defaultsFile,
        copyrightFile,
        changelogFile,
        initScript,
        unixHome,
        unixLibDest,
        unixScriptDest,
        unixLogDir,
        libMappings,
        confMappings,
        scriptMappings
      )
      logger.value info msg
    }
  )
  val linuxSettings: Seq[Setting[_]] = UnixPlugin.unixSettings ++ AzurePlugin.azureSettings ++ Seq(
    linux.Keys.maintainer := "Firstname Lastname <email@address.com>",

    /**
     * Source settings
     */
    printPaths := {
      val keys = Seq(controlDir, preInstall, postInstall, preRemove, postRemove)
      val values = Seq(controlDir.value, preInstall.value, postInstall.value, preRemove.value, postRemove.value)
      val pairs = keys zip values
      PackagingUtil.logPairs(pairs, streams.value)
      values
    },

    /**
     * Destination settings
     */
    // Linux directory layout
    unixHome := Paths get ("/opt/" + (name in Linux).value),
    unixLibDest := unixHome.value / libDir,
    unixScriptDest := unixHome.value / scriptDir,
    unixLogDir := unixHome.value / logDir,
    // rpm/deb postinst control files
    controlDir := (pkgHome in Linux).value / "control",
    preInstall := controlDir.value / "preinstall.sh",
    postInstall := controlDir.value / "postinstall.sh",
    preRemove := controlDir.value / "preuninstall.sh",
    postRemove := controlDir.value / "postuninstall.sh",
    copyrightFile := (pkgHome in Linux).value / "copyright",
    changelogFile := (pkgHome in Linux).value / "changelog",
    initScript := (pkgHome in Linux).value / ((name in Linux).value + ".sh"),
    defaultsFile := (pkgHome in Linux).value / ((name in Linux).value + ".defaults"),
    // Flat copy of libs to /lib on destination system
    libMappings := libs.value.map(file => file -> (unixLibDest.value / file.getFileName).toString),
    scriptMappings := rebase(scriptFiles.value, scriptPath.value, unixScriptDest.value),
    linux.Keys.packageDescription in Linux := "This is the description of the package."
  ) ++ inConfig(Linux)(distroSettings ++ Seq(
    configDestDir := unixHome.value / confDir,
    libDestDir := unixHome.value / libDir,
    confMappings := rebase(configFiles.value, configSrcDir.value, configDestDir.value),
    linux.Keys.packageSummary := s"This is a summary of ${name.value}",
    verifySettings := PackagingUtil.verifyPathSetting(
      controlDir -> controlDir.value,
      preInstall -> preInstall.value,
      postInstall -> postInstall.value,
      preRemove -> preRemove.value,
      postRemove -> postRemove.value,
      defaultsFile -> defaultsFile.value,
      copyrightFile -> copyrightFile.value,
      changelogFile -> changelogFile.value,
      initScript -> initScript.value
    )))
  // TODO improve readability
  val linuxMappings: Seq[Setting[_]] = Seq(
    linux.Keys.linuxPackageMappings ++= {
      val linuxPkgHome = (pkgHome in Linux).value
      Seq(
        fileMap(appJar.value -> (unixHome.value / appJarName.value).toString),
        baseMaps(Seq(
          (linuxPkgHome / libDir) -> unixLibDest.value.toString,
          (linuxPkgHome / logDir) -> unixLogDir.value.toString
        ), perms = "0750"),
        fileMaps(libMappings.value)
      ) ++ pkgMaps(Seq(
        initScript.value -> ("/etc/init.d/" + (name in Linux).value)
      ) ++ scriptMappings.value, perms = "0755") ++
        pkgMaps((confFile in Linux).value.map(cFile => Seq(cFile -> (unixHome.value / cFile.getFileName).toString))
          .getOrElse(Seq.empty[(Path, String)]), perms = "0600", isConfig = true) ++
        pkgMaps((confMappings in Linux).value ++ Seq(defaultsFile.value -> ("/etc/default/" + (name in Linux).value)),
          perms = "0640", isConfig = true)
    }
  )

  val debianSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Debian)(distroSettings ++ linuxMappings) ++ Seq(
    //    debian.Keys.linuxPackageMappings <++= linux.Keys.linuxPackageMappings in Linux,
    AzureKeys.azurePackage in Debian := Some((packageBin in Debian).value.toPath),
    configDestDir in Debian := (configDestDir in Linux).value,
    libDestDir in Debian := (configDestDir in Linux).value,
    //    debian.Keys.version := "0.1",
    // rpm:maintainer defaults to linux:maintainer, but debian:maintainer is empty (?), this fixes that
    debian.Keys.maintainer in Debian := linux.Keys.maintainer.value,
    debian.Keys.linuxPackageMappings in Debian ++= Seq(
      // http://lintian.debian.org/tags/no-copyright-file.html
      fileMap(copyrightFile.value -> ("/usr/share/doc/" + name.value + "/copyright")),
      fileMap(changelogFile.value -> ("/usr/share/doc/" + name.value + "/changelog.gz"), gzipped = true) asDocs(),
      fileMaps(Seq(
        preInstall.value -> "DEBIAN/preinst",
        postInstall.value -> "DEBIAN/postinst",
        preRemove.value -> "DEBIAN/prerm",
        postRemove.value -> "DEBIAN/postrm"
      ), perms = "0755")
    )
  )

  val rpmSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Rpm)(distroSettings ++ linuxMappings) ++ Seq(
    //    rpm.Keys.linuxPackageMappings in Rpm <++= linux.Keys.linuxPackageMappings in Linux,
    AzureKeys.azurePackage in Rpm := Some((packageBin in Rpm).value.toPath),
    configDestDir in Rpm := (configDestDir in Linux).value,
    libDestDir in Rpm := (configDestDir in Linux).value,
    rpm.Keys.rpmVendor := GenericKeys.manufacturer.value,
    rpm.Keys.rpmLicense := Some("All rights reserved."),
    rpm.Keys.rpmPre := fileToString(preInstall.value),
    rpm.Keys.rpmPost := fileToString(postInstall.value),
    rpm.Keys.rpmPreun := fileToString(preRemove.value),
    rpm.Keys.rpmPostun := fileToString(postRemove.value)
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
