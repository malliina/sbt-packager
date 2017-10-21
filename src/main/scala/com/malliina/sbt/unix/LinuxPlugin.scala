package com.malliina.sbt.unix

import java.nio.file.{Files, Path, Paths}

import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.GenericPlugin.{confSettings, confSpecificSettings, genericSettings}
import com.malliina.sbt.unix.LinuxKeys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader
import com.typesafe.sbt.packager.linux.LinuxPackageMapping
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageTemplateMapping
import sbt.Keys._
import sbt._

import scala.io.Source
import scala.sys.process.Process

object LinuxPlugin extends AutoPlugin {
  override def requires = JavaServerAppPackaging

  object autoImport extends LinuxKeys

  override def projectSettings = playSettings

  lazy val playSettings = linuxNativeSettings ++ ciSettings ++ inConfig(Linux) {
    Seq(
      httpPort := Option("8456"),
      httpsPort := None,
      pidFile := Option(s"${(runDir in Linux).value}/${(name in Linux).value}.pid"),
      javaOptions in Universal ++= {
        val logs = (logsDir in Linux).value
        val always = Seq(
          s"-Dlog.dir=$logs",
          "-Dfile.encoding=UTF-8",
          "-Dsun.jnu.encoding=UTF-8"
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

  lazy val linuxNativeSettings =
    genericSettings ++
      confSettings ++
      debianConfSettings ++
      rpmConfSettings ++ Seq(
      maintainer := "Firstname Lastname <email@address.com>",
      pkgHome in Linux := (pkgHome in UnixPlugin.Unix).value,
      appHome in Linux := s"/var/lib/${(name in Linux).value}",
      runDir in Linux := s"/var/run/${(name in Linux).value}",
      logsDir in Linux := s"/var/log/${(name in Linux).value}",
      javaOptions in Universal ++= {
        val linuxName = (name in Linux).value
        val home = (appHome in Linux).value
        Seq(s"-D$linuxName.home=$home")
      },
      linuxPackageMappings ++= {
        // adds empty dir
        Seq((appHome in Linux).value, (runDir in Linux).value, (logsDir in Linux).value).map { dir =>
          packageTemplateMapping(dir)()
            .withUser((daemonUser in Linux).value)
            .withGroup((daemonGroup in Linux).value)
        }
      },
      rpmLicense := Option("MIT License"),
      serverLoading in Debian := Option(ServerLoader.Systemd)
    )

  lazy val debianConfSettings = inConfig(Debian)(confSpecificSettings ++ Seq(
    deployFiles := destPaths(linuxPackageMappings.value)
  ))

  lazy val rpmConfSettings = inConfig(Rpm)(confSpecificSettings ++ Seq(
    deployFiles := destPaths(linuxPackageMappings.value)
  ))

  lazy val ciSettings = Seq(
    ciBuild := {
      val log = streams.value.log
      val file = (packageBin in Debian).value
      val lintianExitValue = Process(Seq("lintian", "-c", "-v", file.getName), Some(file.getParentFile)).!
      if (lintianExitValue > 1) {
        sys.error(s"Invalid lintian exit value: '$lintianExitValue'.")
      }
      val destName = s"${(name in Debian).value}.${file.ext}"
      val destFile = file.getParentFile / destName
      val success = file.renameTo(file.getParentFile / destName)
      if (!success) {
        sys.error(s"Unable to rename '$file' to '$destFile'.")
      } else {
        log.info(s"Renamed '$file' to '$destFile'.")
        destFile
      }
    }
  )

  def fileToString(file: Path) =
    if (Files exists file) {
      Some(Source.fromFile(file.toFile).getLines().mkString("\n"))
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
