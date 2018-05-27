import com.malliina.sbtutils.SbtProjects

lazy val p = SbtProjects.sbtPlugin("sbt-packager")

scalaVersion := "2.12.6"
scalacOptions := Seq("-unchecked", "-deprecation")
organization := "com.malliina"
exportJars := false
resolvers ++= Seq(
  Resolver.bintrayRepo("malliina", "maven"),
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
libraryDependencies ++= Seq(
  "com.malliina" %% "util" % "2.10.2",
  "com.malliina" %% "appbundler" % "1.2.0"
)
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.4")
