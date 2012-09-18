import sbt.Keys._
import sbt._

object BuildBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")
  )
  lazy val root = Project("build", file("."))
}