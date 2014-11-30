package com.mle.sbt.mac

import java.nio.file.Path
import sbt._

/**
 * @author mle
 */
object MacKeys {
  val plistFile = settingKey[Path]("Path to .plist file")
  val dmg = taskKey[Path]("Packages the app to a .dmg file")
}
