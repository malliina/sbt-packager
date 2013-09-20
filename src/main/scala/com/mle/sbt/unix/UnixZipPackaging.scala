package com.mle.sbt.unix

import UnixZipKeys._
import sbt.Keys._
import java.nio.file.{StandardCopyOption, Files}
import sbt._
import UnixKeys._
import com.mle.sbt.PackagingUtil._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.GenericKeys._

/**
 *
 * @author mle
 */
object UnixZipPackaging {
  val outDir = "distrib"
  val unixZipSettings = UnixPlugin.unixSettings ++ Seq(
    /**
     * Destination settings
     */
    distribDir := basePath.value / outDir,
    copyConfs <<= copyTask(configFiles),
    copyScripts <<= copyTask(scriptFiles),
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
