import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override lazy val settings = super.settings ++ Seq(
    name := "sbt-packager",
    sbtPlugin := true,
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
}