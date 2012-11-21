package com.mle.sbt.unix

import com.mle.sbt.GenericKeys._
import com.mle.sbt.PackagingUtil._
import UnixKeys._
import com.mle.sbt.FileImplicits._
import sbt._
import com.mle.sbt.GenericPackaging

/**
 *
 * @author mle
 */
object UnixPackaging {
  val unixSettings: Seq[Setting[_]] = GenericPackaging.genericSettings ++ Seq(
    unixPkgHome <<= (pkgHome)(_ / "unix"),
    configPath <<= (unixPkgHome)(b => Some((b / confDir))),
    scriptPath <<= (unixPkgHome)(b => Some((b / scriptDir))),
    configFiles <<= filesIn(configPath),
    scriptFiles <<= filesIn(scriptPath)
  )
}
