ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

val p = Project("sbt-packager", file("."))
  .enablePlugins(BintrayReleasePlugin)
  .settings(
    scalaVersion := "2.12.10",
    scalacOptions := Seq("-unchecked", "-deprecation"),
    organization := "com.malliina",
    exportJars := false,
    resolvers ++= Seq(
      Resolver.bintrayRepo("malliina", "maven")
    ),
    libraryDependencies ++= Seq(
      "com.malliina" %% "util-base" % "1.12.3",
      "com.malliina" %% "appbundler" % "1.5.0"
    ),
    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.5.2")
  )
