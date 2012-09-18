import sbt.Keys._
import sbt._

object MyBuild extends Build{
   lazy val sbtPackager=Project("sbt-packager",file("."))
}