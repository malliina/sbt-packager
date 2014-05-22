import com.mle.sbtutils.SbtUtils
import com.mle.sbtutils.SbtUtils._
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = SbtUtils.testableProject("sbt-packager").settings(packagerSettings: _*)

  val utilGroup = "com.github.malliina"
  val utilVersion = "1.3.1"
  val utilDep = utilGroup %% "util" % utilVersion
  val azure = utilGroup %% "util-azure" % utilVersion

  val releaseVersion = "1.2.2"
  val snapshotVersion = "1.2.2-SNAPSHOT"

  lazy val packagerSettings = publishSettings ++ Seq(
    scalaVersion := "2.10.4",
//    crossScalaVersions := Seq("2.11.0", "2.10.4"),
    organization := "com.github.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    version := releaseVersion,
    sbtPlugin := true,
    exportJars := false,
    resolvers += "Sonatype snaps" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(utilDep, azure),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")
  )
}