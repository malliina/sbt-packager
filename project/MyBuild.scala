import bintray.BintrayKeys.{bintray => bintrayConf, bintrayOrganization, bintrayRepository}
import com.mle.sbtutils.SbtProjects
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = SbtProjects.testableProject("sbt-packager")
    .enablePlugins(bintray.BintrayPlugin).settings(packagerSettings: _*)

  val mleGroup = "com.github.malliina"
  val utilVersion = "1.5.0"
  val releaseVersion = "1.8.7"

  lazy val packagerSettings = Seq(
    version := releaseVersion,
    scalaVersion := "2.10.4",
    organization := "com.github.malliina",
    sbtPlugin := true,
    exportJars := false,
    libraryDependencies ++= Seq(
      mleGroup %% "util" % utilVersion,
      mleGroup %% "util-azure" % utilVersion,
      mleGroup %% "appbundler" % "0.8.1"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.1"),
    bintrayOrganization in bintrayConf := None,
    bintrayRepository in bintrayConf := "sbt-plugins",
    publishMavenStyle := false,
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}
