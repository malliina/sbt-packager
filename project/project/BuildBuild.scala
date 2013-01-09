import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.9.2",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT")
  )
  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}