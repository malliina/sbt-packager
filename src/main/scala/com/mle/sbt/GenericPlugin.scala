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
    pkgHome := (basePath.value / "src" / "pkg"),
    basePath := baseDirectory.value.toPath,
    appJar := (packageBin in Compile).value.toPath,
    appJarName := name.value + ".jar",
    homeVar := name.value.toUpperCase + "_HOME",
    libs := {
      val deps = (dependencyClasspath in Runtime).value
      val exported = (exportedProducts in Compile).value
      // Libs, but not my own jars
      deps.files.filter(f => !f.isDirectory && !exported.files.contains(f)).map(_.toPath)
    },
    printLibs := libs.value foreach println,
    confFile := None,
    configSrcDir := basePath.value / confDir,
    configFiles <<= listFiles(configSrcDir),
    targetPath := target.value.toPath,
    logger := streams.value.log,
    help := {
      import SbtNativePackager._
      def suggestTask(conf: Configuration) = conf.name + ":helpme"
      val winHelp = suggestTask(Windows)
      val debHelp = suggestTask(Debian)
      val rpmHelp = suggestTask(Rpm)
      val taskList = Seq(winHelp, debHelp, rpmHelp).mkString(FileUtilities.lineSep, FileUtilities.lineSep, FileUtilities.lineSep)
      val helpMsg = describe(pkgHome, appJar, libs, confFile)
      val confMsg = s"Three OS configurations are available: ${Windows.name}, ${Debian.name}, and ${Rpm.name}."
      val suggest = "Try the following: " + taskList
      val msg = Seq(helpMsg, confMsg, suggest).mkString(FileUtilities.lineSep + FileUtilities.lineSep)
      logger.value.info(msg)
    }
  )
  val confSpecificSettings: Seq[Setting[_]] = Seq(
    printFiles := deployFiles.value foreach (dest => logger.value.info(dest.toString)),
    targetPath := target.value.toPath,
    azureUpload := {
      val path: Path = azurePackage.value.getOrElse(throw new Exception(azurePackage.key.label + " not defined."))
      val srcPath = path.toAbsolutePath.toString
      logger.value info s"Uploading to Azure: $srcPath"
      val uri = azureContainer.value.upload(path)
      logger.value info s"Uploaded $srcPath to $uri"
      uri
    }
  )
  val confSettings: Seq[Setting[_]] = Seq(
    confFile := Some(pkgHome.value / (name.value + ".conf"))
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