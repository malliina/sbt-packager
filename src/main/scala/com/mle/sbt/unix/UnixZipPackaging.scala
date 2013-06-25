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
    distribDir <<= (basePath)(b => b / outDir),
    copyConfs <<= copyTask(configFiles),
    copyScripts <<= copyTask(scriptFiles),
    packageApp <<= (
      copyLibs,
      createJar,
      copyConfs,
      copyScripts
      ) map ((libs, jars, confs, scripts) => {
      libs ++ jars ++ confs ++ scripts
    }),
    // TODO: bat is not for unix, Michael
    bat <<= (distribDir, name, packageApp, copyScripts, streams) map ((appDir, appName, appFiles, scripts, logger) => {
      launcher(appDir, scripts, appName, ".bat", appFiles,logger)
    }),
    sh <<= (distribDir, name, packageApp, copyScripts, streams) map ((appDir, appName, appFiles, scripts, logger) => {
      launcher(appDir, scripts, appName, ".sh", appFiles,logger)
    }),
    zip <<= (
      baseDirectory,
      packageApp,
      distribDir,
      name,
      streams
      ) map ((base, files, distribDir, appName, logger) => {
      Files.createDirectories(distribDir)
      val zipFile = base / outDir / (appName + ".zip")
      val rebaser = sbt.Path.rebase(distribDir.toFile, "")
      val filez = files.map(_.toFile)
      IO.zip(filez.map(f => (f, rebaser(f).get)), zipFile)
      logger.log("Packaged: " + zipFile)
      zipFile
    }),

    copyLibs <<= (
      libs,
      distribDir
      ) map ((libJars, dest) => {
      val libDestination = dest resolve libDir
      Files.createDirectories(libDestination)
      libJars.map(libJar => Files.copy(libJar, libDestination resolve libJar.getFileName, StandardCopyOption.REPLACE_EXISTING))
    }),
    createJar <<= (
      exportedProducts in Compile,
      distribDir,
      name,
      version,
      scalaVersion
      ) map ((products, dest, appName, appVer, scalaVer) => {
      Files.createDirectories(dest)
      val versionSuffix = "_" + scalaVer + "-" + appVer
      val jarPaths = products.files.map(_.toPath)
      jarPaths.map(jarPath => {
        Files.copy(jarPath, (dest / stripSection(jarPath.getFileName.toString, versionSuffix)), StandardCopyOption.REPLACE_EXISTING)
      })
    })
  )
}
