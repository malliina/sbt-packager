import sbt.Keys._
import sbt._

object MyBuild extends Plugin {

  override def buildSettings = super.buildSettings ++ Seq(
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    sbtPlugin := true,
    name := "sbt-packager",
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
  ) ++ org.sbtidea.SbtIdeaPlugin.settings


//  override def projectSettings = super.projectSettings ++ Seq(
//    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
//  )

  val commonSettings = Defaults.defaultSettings ++ Seq(
    exportJars := true,
    retrieveManaged := true
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(name := "sbt-packager")
}