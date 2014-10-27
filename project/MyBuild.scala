import com.mle.sbtutils.SbtProjects
import com.mle.sbtutils.SbtUtils._
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = SbtProjects.mavenPublishProject("sbt-packager").settings(packagerSettings: _*)

  val utilGroup = "com.github.malliina"
  val utilVersion = "1.5.0"
  val utilDep = utilGroup %% "util" % utilVersion
  val azure = utilGroup %% "util-azure" % utilVersion

  val releaseVersion = "1.3.0"
  val snapshotVersion = "1.2.2-SNAPSHOT"

  lazy val packagerSettings = publishSettings ++ Seq(
    scalaVersion := "2.10.4",
//    crossScalaVersions := Seq("2.11.2", "2.10.4"),
    organization := "com.github.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    version := releaseVersion,
    sbtPlugin := true,
    exportJars := false,
    resolvers ++= Seq(
      "typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
      "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
    ),
    libraryDependencies ++= Seq(utilDep, azure),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")
  )
}