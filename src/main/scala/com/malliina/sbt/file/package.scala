package com.malliina.sbt

import java.nio.file.Path

package object file {
  implicit class PathOps(path: Path) {
    def /(other: String): Path = path.resolve(other)

    def /(other: Path): Path = path.resolve(other)
  }
}
