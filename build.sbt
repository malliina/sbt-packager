import com.malliina.sbtutils.SbtProjects

lazy val p = SbtProjects.sbtPlugin("sbt-packager")

scalaVersion := "2.12.7"
scalacOptions := Seq("-unchecked", "-deprecation")
organization := "com.malliina"
exportJars := false
resolvers ++= Seq(
  Resolver.bintrayRepo("malliina", "maven"),
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
libraryDependencies ++= Seq(
  "com.malliina" %% "util-base" % "1.6.0",
  "com.malliina" %% "appbundler" % "1.2.0"
)
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.7")
