import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.0",
    organization := "com.mle",
    name := "sbt-packager",
    version := "0.7-SNAPSHOT",
    sbtPlugin := true,
    exportJars := true
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
}