import sbt.Keys._
import sbt._

object MyBuild extends Build {
  val utilDep = "com.github.malliina" %% "util" % "0.64-SNAPSHOT"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.9.2",
    organization := "com.github.malliina",
    name := "sbt-packager",
    version := "0.960-SNAPSHOT",
    sbtPlugin := true,
    exportJars := false,
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )
  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(libraryDependencies ++= Seq(utilDep))
}