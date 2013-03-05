package com.mle.sbt

import com.mle.sbt.GenericKeys._
import sbt._
import sbt.Keys._
import FileImplicits._
import java.nio.file.Path
import com.mle.util.FileUtilities
import scala.Some
import com.mle.sbt.PackagingUtil._
import scala.Some

object GenericPlugin extends Plugin {
  val genericSettings: Seq[Setting[_]] = Seq(
    pkgHome <<= (basePath)(_ / "src" / "pkg"),
    basePath <<= (baseDirectory)(_.toPath),
    appJar <<= (packageBin in Compile, name) map ((jarFile, pkgName) => jarFile.toPath),
    appJarName <<= (name)(_ + ".jar"),
    homeVar <<= (name)(_.toUpperCase + "_HOME"),
    libs <<= (
      dependencyClasspath in Runtime,
      exportedProducts in Compile
      ) map ((cp, products) => {
      // Libs, but not my own jars
      cp.files.filter(f => !f.isDirectory && !products.files.contains(f)).map(_.toPath)
    }),
    printLibs <<= (libs, name) map ((l: Seq[Path], pkgName) => {
      l foreach println
    }),
    confFile := None,
    configPath <<= (basePath)(_ / confDir),
    configFiles <<= listFiles(configPath)
  )
  val confSettings: Seq[Setting[_]] = Seq(
    confFile <<= (pkgHome, name)((w, n) => Some(w / (n + ".conf")))
  )
}