import sbt.Keys._
import sbt._

object MyBuild extends Build {
  val utilDep = "com.mle" %% "util" % "0.63-SNAPSHOT"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.9.2",
    organization := "com.mle",
    name := "sbt-packager",
    version := "0.92-SNAPSHOT",
    sbtPlugin := true,
    exportJars := true
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(libraryDependencies ++= Seq(utilDep))
}