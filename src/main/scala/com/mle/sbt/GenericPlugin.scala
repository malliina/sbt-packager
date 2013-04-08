package com.mle.sbt

import com.mle.sbt.GenericKeys._
import sbt._
import sbt.Keys._
import java.nio.file.Path
import com.mle.sbt.PackagingUtil._
import com.mle.sbt.FileImplicits._
import com.mle.sbt.azure.AzureKeys._
import scala.Some
import java.lang.Exception


object GenericPlugin extends Plugin {
  val genericSettings: Seq[Setting[_]] = Seq(
    pkgHome <<= (basePath)(_ / "src" / "pkg"),
    basePath <<= (baseDirectory)(_.toPath),
    appJar <<= (packageBin in Compile, name) map ((jarFile, pkgName) => jarFile.toPath),
    appJarName <<= (name)(_ + ".jar"),
    homeVar <<= (name)(_.toUpperCase + "_HOME"),
    libs <<= (
      dependencyClasspath in Runtime,
      exportedProducts in Compile
      ) map ((cp, products) => {
      // Libs, but not my own jars
      cp.files.filter(f => !f.isDirectory && !products.files.contains(f)).map(_.toPath)
    }),
    printLibs <<= (libs, name) map ((l: Seq[Path], pkgName) => {
      l foreach println
    }),
    confFile := None,
    configSrcDir <<= (basePath)(_ / confDir),
    configFiles <<= listFiles(configSrcDir),
    targetPath <<= (target)(_.toPath),
    versionFile <<= (targetPath)(_ / "version.txt"),
    logger <<= (streams) map ((s: Keys.TaskStreams) => s.log)
  )
  val confSpecificSettings: Seq[Setting[_]] = Seq(
    pathMappings <<= (version, versionFile, configDestDir) map ((v, vFile, confDest) => {
      // reads version setting, writes it to file, includes it in app distribution
      PackagingUtil.writerTo(vFile)(_.println(v))
      Seq(vFile -> confDest / vFile.getFileName)
    }),
    printFiles <<= (deployFiles, streams) map ((destFiles, logger) => {
      destFiles foreach (dest => logger.log.info(dest.toString))
    }),
    targetPath <<= target(_.toPath),
    uploadRelease <<= (azureContainer, azurePackage, logger) map ((container, file, log) => {
      val uri = file.map(container.upload)
        .getOrElse(throw new Exception(azurePackage.key.label + " not defined."))
      log.info("Uploaded package to " + uri)
      uri
    })
  )
  val confSettings: Seq[Setting[_]] = Seq(
    confFile <<= (pkgHome, name)((w, n) => Some(w / (n + ".conf")))
  )
}