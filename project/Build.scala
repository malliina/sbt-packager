import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override lazy val settings = super.settings ++ Seq(
    organization := "com.mle",
    name := "sbt-packager",
    version := "0.3-SNAPSHOT",
    sbtPlugin := true,
    exportJars := true,
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
}