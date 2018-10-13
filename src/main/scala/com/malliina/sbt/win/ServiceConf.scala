package com.malliina.sbt.win

import java.nio.file.Path

case class ServiceConf(serviceExe: Path, exeName: String, runtimeConf: Path, xmlConf: Path) {
  val exeConfigName = exeName + ".config"
  val exeConfigSource = serviceExe.getParent.resolve(exeName + ".config")
}
