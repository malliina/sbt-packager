import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.10.4",
    resolvers ++= Seq(
      "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      Resolver.url("scalasbt", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
      Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins/"))(Resolver.ivyStylePatterns))
  ) ++ sbtPlugins

  def sbtPlugins = Seq(
    "com.github.malliina" %% "sbt-utils" % "0.2.0",
    "me.lessis" % "bintray-sbt" % "0.3.0"
  ) map addSbtPlugin

  //  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}