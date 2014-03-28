import com.mle.sbtutils.SbtUtils._
import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = Project("sbt-packager", file(".")).settings(packagerSettings: _*)

  val utilGroup = "com.github.malliina"
  val utilVersion = "1.0.0"
  val utilDep = utilGroup %% "util" % utilVersion
  val azure = utilGroup %% "util-azure" % utilVersion
  val scalaTest = "org.scalatest" %% "scalatest" % "2.0" % "test"

  val releaseVersion = "1.2.1"
  val snapshotVersion = "1.2.2-SNAPSHOT"

  lazy val packagerSettings = publishSettings ++ Seq(
    scalaVersion := "2.10.3",
    organization := "com.github.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    version := snapshotVersion,
    sbtPlugin := true,
    exportJars := false,
    resolvers += "Sonatype snaps" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= Seq(utilDep, scalaTest, azure),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2")
  )
}