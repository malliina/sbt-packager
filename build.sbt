lazy val p = Project("sbt-packager", file("."))
  .enablePlugins(BintrayReleasePlugin)

scalaVersion := "2.12.8"
scalacOptions := Seq("-unchecked", "-deprecation")
organization := "com.malliina"
exportJars := false
resolvers ++= Seq(
  Resolver.bintrayRepo("malliina", "maven"),
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
libraryDependencies ++= Seq(
  "com.malliina" %% "util-base" % "1.8.1",
  "com.malliina" %% "appbundler" % "1.3.0"
)
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.7")
