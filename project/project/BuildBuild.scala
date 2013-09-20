import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.2",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
  ) ++ sbtPlugins

  def sbtPlugins = Seq(
    "com.github.mpeltonen" % "sbt-idea" % "1.5.1",
    "com.typesafe.sbt" % "sbt-pgp" % "0.8"
  ) map addSbtPlugin

  //  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}