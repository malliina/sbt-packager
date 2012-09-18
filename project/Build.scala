import sbt.Keys._
import sbt._

object MyBuild extends Plugin{

  override def projectSettings = super.projectSettings ++ Seq(
    sbtPlugin := true
  )

  lazy val sbtPackager=Project("sbt-packager",file("."))
}