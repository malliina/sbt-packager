import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(libraryDependencies ++= Seq(utilDep, scalaTest, azure))
    .settings(addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.2"))

  val utilGroup = "com.github.malliina"
  val utilVersion = "1.0.0"
  val utilDep = utilGroup %% "util" % utilVersion
  val azure = utilGroup %% "util-azure" % utilVersion
  val scalaTest = "org.scalatest" %% "scalatest" % "1.9.2" % "test"

  val releaseVersion = "1.1.2"
  val snapshotVersion = "1.2.0-SNAPSHOT"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.2",
    organization := "com.github.malliina",
    name := "sbt-packager",
    version := snapshotVersion,
    sbtPlugin := true,
    exportJars := false,
    resolvers += "Sonatype snaps" at "http://oss.sonatype.org/content/repositories/snapshots/",
    publishTo := {
      val repo =
        if (version.value endsWith "SNAPSHOT") {
          "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
        } else {
          "Sonatype releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
        }
      Some(repo)
    },
    licenses += ("BSD-style" -> url("http://www.opensource.org/licenses/BSD-3-Clause")),
    scmInfo := Some(ScmInfo(url("https://github.com/malliina/sbt-packager"), "git@github.com:malliina/sbt-packager.git")),
    credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.txt"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := extraPom
  )

  def extraPom = (
    <url>https://github.com/malliina/sbt-packager</url>
      <developers>
        <developer>
          <id>malliina</id>
          <name>Michael Skogberg</name>
          <url>http://mskogberg.info</url>
        </developer>
      </developers>)
}