scalaVersion := "2.12.10"
resolvers ++= Seq(
  Resolver.url(
    "scalasbt",
    url("https://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases")
  )(Resolver.ivyStylePatterns),
  Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins/"))(
    Resolver.ivyStylePatterns
  )
)
Seq(
  "com.malliina" %% "sbt-utils-bintray" % "0.15.1",
  "ch.epfl.scala" % "sbt-bloop" % "1.3.4",
  "org.scalameta" % "sbt-scalafmt" % "2.3.0"
) map addSbtPlugin
