import com.mle.sbtutils.SbtProjects
import com.mle.sbtutils.SbtUtils._
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = SbtProjects.mavenPublishProject("sbt-packager").settings(packagerSettings: _*)

  val mleGroup = "com.github.malliina"
  val utilVersion = "1.5.0"
  val releaseVersion = "1.5.11"

  lazy val packagerSettings = publishSettings ++ Seq(
    version := releaseVersion,
    scalaVersion := "2.10.4",
    organization := "com.github.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    sbtPlugin := true,
    exportJars := false,
    resolvers ++= Seq(
      "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
    ),
    libraryDependencies ++= Seq(
      mleGroup %% "util" % utilVersion,
      mleGroup %% "util-azure" % utilVersion,
      mleGroup %% "appbundler" % "0.8.1"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")
  )
}