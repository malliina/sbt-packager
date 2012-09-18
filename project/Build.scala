import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override def settings = super.settings ++ Seq(
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    sbtPlugin := true,
    name := "sbt-packager",
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
}