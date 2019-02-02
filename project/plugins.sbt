scalaVersion := "2.12.8"
resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.url("scalasbt", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
  Resolver.url("malliina bintray sbt", url("https://dl.bintray.com/malliina/sbt-plugins/"))(Resolver.ivyStylePatterns)
)
Seq(
  "com.malliina" %% "sbt-utils-bintray" % "0.10.1"
) map addSbtPlugin
