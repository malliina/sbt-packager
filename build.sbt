ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val p = Project("sbt-packager", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.11",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    exportJars := false,
    resolvers ++= Seq(
      Resolver.bintrayRepo("malliina", "maven")
    ),
    libraryDependencies ++= Seq(
      "com.malliina" %% "util-base" % "1.17.0",
      "com.malliina" %% "appbundler" % "1.6.0"
    ),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.2")
  )
