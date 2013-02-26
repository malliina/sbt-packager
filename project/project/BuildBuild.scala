import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    scalaVersion := "2.9.2",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0-SNAPSHOT"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8")

  )
  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file("."))
}