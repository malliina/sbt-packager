package com.malliina.sbt

import com.malliina.sbt.GenericKeys._
import sbt.Keys._
import sbt._

object GenericPlugin {
  val lineSep = PackagingUtil.lineSep
  val genericSettings: Seq[Setting[_]] = Seq(
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
    configFiles := PackagingUtil.listFiles(configSrcDir).value,
    targetPath := target.value.toPath,
    logger := streams.value.log,
    helpMe := {
      import com.typesafe.sbt.SbtNativePackager._
      def suggestTask(conf: Configuration) = conf.name + ":helpme"

      val winHelp = suggestTask(Windows)
      val debHelp = suggestTask(Debian)
      val rpmHelp = suggestTask(Rpm)
      val taskList = Seq(winHelp, debHelp, rpmHelp).mkString(lineSep, lineSep, lineSep)
      val helpMsg = describe(pkgHome, appJar, libs, confFile)
      val confMsg =
        s"Three OS configurations are available: ${Windows.name}, ${Debian.name}, and ${Rpm.name}."
      val suggest = "Try the following: " + taskList
      val msg = Seq(helpMsg, confMsg, suggest).mkString(lineSep + lineSep)
      logger.value.info(msg)
    }
  )
  val confSpecificSettings: Seq[Setting[_]] = Seq(
    printFiles := {
      val log = logger.value
      deployFiles.value foreach (dest => log.info(dest.toString))
    },
    targetPath := target.value.toPath
  )
  val confSettings: Seq[Setting[_]] = Seq(
    confFile := Some(pkgHome.value / (name.value + ".conf"))
  )

  def describe(tasks: ScopedTaskable[_]*) = tasks
    .map(_.key)
    .map(t => {
      val tabCount = t.label.length match {
        case i if i > 16 => 1
        case i if i > 8  => 2
        case _           => 3
      }
      val sep = (1 to tabCount).map(_ => "\t").mkString
      t.label + sep + t.description.getOrElse("No description")
    })
    .mkString(lineSep)

  def describeWithAzure(tasks: ScopedTaskable[_]*) = describe(tasks: _*)
}
