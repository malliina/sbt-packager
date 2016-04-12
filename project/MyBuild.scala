import bintray.BintrayKeys.{bintray => bintrayConf, bintrayOrganization, bintrayRepository}
import com.malliina.sbtutils.SbtProjects
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = SbtProjects.testableProject("sbt-packager")
    .enablePlugins(bintray.BintrayPlugin)
    .settings(packagerSettings: _*)

  val malliinaGroup = "com.malliina"
  val utilVersion = "2.2.3"
  val releaseVersion = "2.1.0"

  lazy val packagerSettings = Seq(
    version := releaseVersion,
    scalaVersion := "2.10.6",
    organization := malliinaGroup,
    sbtPlugin := true,
    exportJars := false,
    resolvers ++= Seq(Resolver.bintrayRepo("malliina", "maven")),
    libraryDependencies ++= Seq(
      malliinaGroup %% "util" % utilVersion,
      malliinaGroup %% "util-azure" % utilVersion,
      malliinaGroup %% "appbundler" % "0.9.2"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.1"),
    bintrayOrganization in bintrayConf := None,
    bintrayRepository in bintrayConf := "sbt-plugins",
    publishMavenStyle := false,
    licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  )
}
