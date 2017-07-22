package com.malliina.sbt.unix

import java.nio.file.{Files, StandardCopyOption}

import com.malliina.file.StorageFile
import com.malliina.sbt.GenericKeys._
import com.malliina.sbt.PackagingUtil._
import com.malliina.sbt.unix.UnixKeys._
import com.malliina.sbt.unix.UnixZipKeys._
import sbt.Keys._
import sbt._

object UnixZipPackaging {
  val outDir = "distrib"
  val unixZipSettings = UnixPlugin.unixSettings ++ Seq(
    /**
      * Destination settings
      */
    distribDir := basePath.value / outDir,
    copyConfs := copyTask(configFiles).value,
    copyScripts := copyTask(scriptFiles).value,
    packageApp := copyLibs.value ++ createJar.value ++ copyConfs.value ++ copyScripts.value,
    // TODO: bat is not for unix, Michael
    bat := launcher(distribDir.value, copyScripts.value, name.value, ".bat", packageApp.value, streams.value),
    sh := launcher(distribDir.value, copyScripts.value, name.value, ".sh", packageApp.value, streams.value),
    zip := {
      Files.createDirectories(distribDir.value)
      val zipFile = baseDirectory.value / outDir / (name.value + ".zip")
      val rebaser = sbt.Path.rebase(distribDir.value.toFile, "")
      val filez = packageApp.value.map(_.toFile)
      IO.zip(filez.map(f => (f, rebaser(f).get)), zipFile)
      streams.value.log("Packaged: " + zipFile)
      zipFile
    },
    copyLibs := {
      val libDestination = distribDir.value resolve libDir
      Files.createDirectories(libDestination)
      libs.value.map(libJar => Files.copy(libJar, libDestination resolve libJar.getFileName, StandardCopyOption.REPLACE_EXISTING))
    },
    createJar := {
      Files.createDirectories(distribDir.value)
      val versionSuffix = "_" + scalaVersion.value + "-" + version.value
      val jarPaths = (exportedProducts in Compile).value.files.map(_.toPath)
      jarPaths.map(jarPath => {
        Files.copy(jarPath, distribDir.value / stripSection(jarPath.getFileName.toString, versionSuffix), StandardCopyOption.REPLACE_EXISTING)
      })
    }
  )
}
