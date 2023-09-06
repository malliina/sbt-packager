ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val p = Project("sbt-packager", file("."))
  .enablePlugins(MavenCentralPlugin)
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.18",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    organization := "com.malliina",
    gitUserName := "malliina",
    developerName := "Michael Skogberg",
    exportJars := false,
    libraryDependencies ++= Seq(
      "com.malliina" %% "util-base" % "3.1.0",
      "com.malliina" %% "appbundler" % "1.7.1"
    ),
    addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
  )
