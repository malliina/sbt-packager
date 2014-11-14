import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns))
  ) ++ sbtPlugins

  def sbtPlugins = Seq(
    "com.github.malliina" %% "sbt-utils" % "0.0.5"
  ) map addSbtPlugin

  //  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}