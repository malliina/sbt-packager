trait SbtHelpers {
  def gitPom(projectName: String, gitUser: String, realName: String, developerHomePage: String) =
    (<url>https://github.com/{gitUser}/{projectName}</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:{gitUser}/{projectName}.git</url>
        <connection>scm:git:git@github.com:{gitUser}/{projectName}.git</connection>
      </scm>
      <developers>
        <developer>
          <id>{gitUser}</id>
          <name>{realName}</name>
          <url>{developerHomePage}</url>
        </developer>
      </developers>)
}
object SbtHelpers extends SbtHelpers