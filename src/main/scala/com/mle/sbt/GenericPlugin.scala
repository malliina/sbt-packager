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
import com.typesafe.sbt.SbtNativePackager
import com.mle.util.FileUtilities
import com.mle.sbt.azure.AzurePlugin


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
    logger <<= (streams) map ((s: Keys.TaskStreams) => s.log),
    help <<= (logger) map (log => {
      import SbtNativePackager._
      def suggestTask(conf: Configuration) = conf.name + ":helpme"
      val winHelp = suggestTask(Windows)
      val debHelp = suggestTask(Debian)
      val rpmHelp = suggestTask(Rpm)
      val taskList = Seq(winHelp, debHelp, rpmHelp).mkString(FileUtilities.lineSep, FileUtilities.lineSep, FileUtilities.lineSep)
      val helpMsg = describe(pkgHome, appJar, libs, confFile, versionFile)
      val confMsg = "Three OS configurations are available: " + Windows.name + ", " + Debian.name + ", and " + Rpm.name
      val suggest = "Try the following: " + taskList
      val msg = Seq(helpMsg, confMsg, suggest).mkString(FileUtilities.lineSep+FileUtilities.lineSep)
      log.info(msg)
    })
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
    azureUpload <<= (azureContainer, azurePackage, logger) map ((container, file, log) => {
      val uri = file.map(container.upload)
        .getOrElse(throw new Exception(azurePackage.key.label + " not defined."))
      log.info("Uploaded package to " + uri)
      uri
    })
  )
  val confSettings: Seq[Setting[_]] = Seq(
    confFile <<= (pkgHome, name)((w, n) => Some(w / (n + ".conf")))
  )

  def describe(tasks: ScopedTaskable[_]*) = tasks.map(_.key).map(t => {
    val tabCount = t.label.size match {
      case i if i > 16 => 1
      case i if i > 8 => 2
      case _ => 3
    }
    val sep = (1 to tabCount).map(_ => "\t").mkString
    t.label + sep + t.description.getOrElse("No description")
  }).mkString(FileUtilities.lineSep)

  def describeWithAzure(tasks: ScopedTaskable[_]*) =
    describe(tasks: _*) + FileUtilities.lineSep + AzurePlugin.describe
}