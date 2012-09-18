import sbt.Keys._
import sbt._

object MyBuild extends Build{

  override def settings = super.settings ++ Seq(
    sbtPlugin := true
  )
  lazy val sbtPackager=Project("sbt-packager",file("."))
}