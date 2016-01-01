package com.malliina.sbt.unix

import java.nio.file.{Files, Path, Paths}

import com.malliina.file.StorageFile
import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.azure.{AzureKeys, AzurePlugin}
import com.malliina.sbt.unix.LinuxKeys._
import com.malliina.sbt.unix.UnixKeys._
import com.malliina.sbt.{GenericKeys, GenericPlugin, PackagingUtil}
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import sbt.Keys._
import sbt._

object LinuxPlugin extends Plugin {
  private val linuxKeys = com.typesafe.sbt.packager.Keys

  val linuxNativeSettings =
    GenericPlugin.genericSettings ++
      GenericPlugin.confSettings ++
      debianConfSettings ++
      rpmConfSettings ++ Seq(
      linuxKeys.maintainer := "Firstname Lastname <email@address.com>",
      pkgHome in Linux := (pkgHome in UnixPlugin.Unix).value,
      appHome in Linux := Option(s"/var/run/${(name in Linux).value}"),
      javaOptions in Universal ++= {
        val linuxName = (name in Linux).value
        (appHome in Linux).value.map(home => s"-D$linuxName.home=$home").toSeq
      },
      linuxKeys.rpmLicense := Option("MIT License")
    )

  lazy val debianConfSettings = inConfig(Debian)(GenericPlugin.confSpecificSettings ++ Seq(
    deployFiles := destPaths(linuxKeys.linuxPackageMappings.value),
    AzureKeys.azurePackage in Debian := Some((packageBin in Debian).value.toPath)
  ))

  lazy val rpmConfSettings = inConfig(Rpm)(GenericPlugin.confSpecificSettings ++ Seq(
    deployFiles := destPaths(linuxKeys.linuxPackageMappings.value),
    AzureKeys.azurePackage in Rpm := Some((packageBin in Rpm).value.toPath)
  ))

  lazy val playSettings = linuxNativeSettings ++ inConfig(Linux) {
    Seq(
      httpPort := Option("8456"),
      httpsPort := None,
      pidFile := appHome.value.map(home => s"$home/${(name in Linux).value}.pid"),
      javaOptions in Universal ++= {
        val linuxName = (name in Linux).value
        val always = Seq(
          s"-Dlog.dir=/var/run/$linuxName/logs"
        )
        val optional = Seq(
          httpPort.value.map(port => s"-Dhttp.port=$port"),
          httpsPort.value.map(sslPort => s"-Dhttps.port=$sslPort"),
          pidFile.value.map(path => s"-Dpidfile.path=$path")
        ).flatten
        always ++ optional
      }
    )
  }

  @deprecated("Use linuxNativeSettings instead")
  val distroSettings = GenericPlugin.confSpecificSettings ++ Seq(
    deployFiles := destPaths(linuxKeys.linuxPackageMappings.value),
    mappingsPrint := printMappings(linuxKeys.linuxPackageMappings.value, streams.value),
    helpMe := {
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

  @deprecated("Use linuxNativeSettings instead")
  val linuxSettings: Seq[Setting[_]] = UnixPlugin.unixSettings ++ AzurePlugin.azureSettings ++ Seq(
    linuxKeys.maintainer := "Firstname Lastname <email@address.com>",
    pkgHome in Linux := (pkgHome in UnixPlugin.Unix).value,

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
    linuxKeys.packageDescription in Linux := "This is the description of the package."
  ) ++ inConfig(Linux)(distroSettings ++ Seq(
    linuxKeys.packageSummary := s"This is a summary of ${name.value}",
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
    linuxKeys.linuxPackageMappings ++= {
      val linuxPkgHome = (pkgHome in Linux).value
      Seq(
        //        fileMap(appJar.value -> (unixHome.value / appJarName.value).toString),
        basePathMaps(Seq(
          (linuxPkgHome / libDir) -> unixLibDest.value,
          (linuxPkgHome / logDir) -> unixLogDir.value
        ), perms = "0750")
      ) ++ pathMaps(libMappings.value) ++ pkgMaps(Seq(
        initScript.value -> ("/etc/init.d/" + (name in Linux).value)
      ) ++ scriptMappings.value.map(p => p._1 -> p._2.toString), perms = "0755") ++
        pkgMaps((confFile in Linux).value.map(cFile => Seq(cFile -> (unixHome.value / cFile.getFileName).toString))
          .getOrElse(Seq.empty[(Path, String)]), perms = "0600", isConfig = true) ++
        pkgMaps((confMappings in Linux).value.map(p => p._1 -> p._2.toString) ++ Seq(defaultsFile.value -> ("/etc/default/" + (name in Linux).value)),
          perms = "0640", isConfig = true)
    }
  )

  @deprecated("Use linuxNativeSettings instead")
  val debianSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Debian)(distroSettings ++ linuxMappings) ++ Seq(
    //    debian.Keys.linuxPackageMappings <++= linux.Keys.linuxPackageMappings in Linux,
    AzureKeys.azurePackage in Debian := Some((packageBin in Debian).value.toPath),
    configDestDir in Debian := (configDestDir in Linux).value,
    libDestDir in Debian := (configDestDir in Linux).value,
    //    debian.Keys.version := "0.1",
    // rpm:maintainer defaults to linux:maintainer, but debian:maintainer is empty (?), this fixes that
    linuxKeys.maintainer in Debian := linuxKeys.maintainer.value,
    linuxKeys.linuxPackageMappings in Debian ++= Seq(
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

  @deprecated("Use linuxNativeSettings instead")
  val rpmSettings: Seq[Setting[_]] = linuxSettings ++ inConfig(Rpm)(distroSettings ++ linuxMappings) ++ Seq(
    //    rpm.Keys.linuxPackageMappings in Rpm <++= linux.Keys.linuxPackageMappings in Linux,
    AzureKeys.azurePackage in Rpm := Some((packageBin in Rpm).value.toPath),
    configDestDir in Rpm := (configDestDir in Linux).value,
    libDestDir in Rpm := (configDestDir in Linux).value,
    linuxKeys.rpmVendor := GenericKeys.manufacturer.value,
    linuxKeys.rpmLicense := Some("All rights reserved."),
    linuxKeys.rpmPre := fileToString(preInstall.value),
    linuxKeys.rpmPost := fileToString(postInstall.value),
    linuxKeys.rpmPreun := fileToString(preRemove.value),
    linuxKeys.rpmPostun := fileToString(postRemove.value)
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

  def basePathMaps(paths: Seq[(Path, Path)], perms: String,
                   user: String = "root", group: String = "root") =
    baseMaps(paths.map(p => p._1 -> p._2.toString), perms, user, group)

  def baseMaps(paths: Seq[(Path, String)], perms: String,
               user: String = "root", group: String = "root") =
    LinuxPackageMapping(paths.map(pair => pair._1.toFile -> pair._2)) withPerms perms withUser user withGroup group

  // TODO fix this nonsense
  //  def pkgPathMaps(paths: Seq[(Path, String)],
  //                  user: String = "root",
  //                  group: String = "root",
  //                  perms: String = "0644",
  //                  dirPerms: String = "0755",
  //                  isConfig: Boolean = false,
  //                  gzipped: Boolean = false) =
  //    pkgMaps(paths.map(p => p._1 -> p._2.toString), user, group, perms, dirPerms, isConfig, gzipped)

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

  def rebase(files: Seq[Path], srcBase: Path, destBase: Path): Seq[(Path, Path)] =
    files map (file => file -> rebase(file, srcBase, destBase))

  def rebase(files: Seq[Path], maybeSrcBase: Option[Path], destBase: Path): Seq[(Path, Path)] =
    maybeSrcBase.map(srcBase => rebase(files, srcBase, destBase)).getOrElse(Seq.empty[(Path, Path)])

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
