import sbt.Keys._
import sbt._

object MyBuild extends Build {
  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(libraryDependencies ++= Seq(utilDep, scalaTest))
    .settings(addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.5.4"))

  val utilDep = "com.github.malliina" %% "util" % "0.7.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val releaseVersion = "0.9.7"
  val snapshotVersion = "0.9.9"

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.9.2",
    organization := "com.github.malliina",
    name := "sbt-packager",
    version := snapshotVersion,
    sbtPlugin := true,
    exportJars := false,
    resolvers += "Sonatype snaps" at "http://oss.sonatype.org/content/repositories/snapshots/",
    publishTo <<= (version)(v => {
      val repo =
        if (v endsWith "SNAPSHOT") {
          "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
        } else {
          "Sonatype releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
        }
      Some(repo)
    }),
    credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.txt"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := extraPom
  )

  def extraPom = (
    <url>https://github.com/malliina/sbt-packager</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:malliina/sbt-packager.git</url>
        <connection>scm:git:git@github.com:malliina/sbt-packager.git</connection>
      </scm>
      <developers>
        <developer>
          <id>malliina</id>
          <name>Michael Skogberg</name>
          <url>http://mskogberg.info</url>
        </developer>
      </developers>)
}