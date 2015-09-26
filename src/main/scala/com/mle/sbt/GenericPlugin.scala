package com.mle.sbt

import java.nio.file.Path

import com.mle.file.FileUtilities
import com.mle.file.StorageFile
import com.mle.sbt.GenericKeys._
import com.mle.sbt.PackagingUtil._
import com.mle.sbt.azure.AzureKeys._
import com.mle.sbt.azure.AzurePlugin
import sbt.Keys._
import sbt._

object GenericPlugin extends Plugin {
  val genericSettings: Seq[Setting[_]] = AzurePlugin.azureSettings ++ Seq(
    pkgHome := (basePath.value / "src" / "pkg"),
    basePath := baseDirectory.value.toPath,
    displayName := name.value,
    appJar := (packageBin in Compile).value.toPath,
    appJarName := name.value + ".jar",
    homeVar := name.value.toUpperCase + "_HOME",
    libs := {
      val deps = (dependencyClasspath in Runtime).value
      deps.files.filter(f => !f.isDirectory).map(_.toPath)
    },
    printLibs := libs.value foreach println,
    confFile := None,
    configSrcDir := basePath.value / confDir,
    configFiles <<= listFiles(configSrcDir),
    targetPath := target.value.toPath,
    logger := streams.value.log,
    helpMe := {
      import com.typesafe.sbt.SbtNativePackager._
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