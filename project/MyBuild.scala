import com.malliina.sbtutils.SbtProjects
import sbt.Keys._
import sbt._

object MyBuild {
  lazy val sbtPackager = SbtProjects.sbtPlugin("sbt-packager")
    .settings(packagerSettings: _*)

  val malliinaGroup = "com.malliina"
  val utilVersion = "2.2.3"

  lazy val packagerSettings = Seq(
    scalaVersion := "2.10.6",
    organization := malliinaGroup,
    exportJars := false,
    resolvers ++= Seq(Resolver.bintrayRepo("malliina", "maven")),
    libraryDependencies ++= Seq(
      malliinaGroup %% "util" % utilVersion,
      malliinaGroup %% "util-azure" % utilVersion,
      malliinaGroup %% "appbundler" % "0.9.3"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.5")
  )
}
