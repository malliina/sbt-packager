import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override lazy val settings = super.settings ++ Seq(
    organization := "com.mle",
    name := "sbt-packager",
    version := "0.6-SNAPSHOT",
    sbtPlugin := true,
    exportJars := true
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
}