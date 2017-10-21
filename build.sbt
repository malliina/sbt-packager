import com.malliina.sbtutils.SbtProjects

lazy val p = SbtProjects.sbtPlugin("sbt-packager")

scalaVersion := "2.12.4"
scalacOptions := Seq("-unchecked", "-deprecation")
organization := "com.malliina"
exportJars := false
resolvers ++= Seq(
  Resolver.bintrayRepo("malliina", "maven"),
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
libraryDependencies += "com.malliina" %% "appbundler" % "1.0.1"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
