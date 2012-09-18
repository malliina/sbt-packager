import sbt.Keys._
import sbt._

object MyBuild extends Build {

  override def settings = super.settings ++ Seq(
    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    sbtPlugin := true,
    name := "sbt-packager",
    addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.4")
  )

  val commonSettings = Defaults.defaultSettings ++ Seq(
    exportJars := true,
    retrieveManaged := true
  )

  lazy val sbtPackager = Project("sbt-packager", file("."))
    .settings(
    name := "sbt-packager"
//    libraryDependencies ++= Seq(packDep)
  )
//  lazy val packDep = "com.typesafe" %% "sbt-native-packager" % "0.4.4"
}