package com.mle.sbt.unix

import com.mle.sbt.GenericKeys._
import com.mle.sbt.PackagingUtil._
import UnixKeys._
import com.mle.sbt.FileImplicits._
import sbt._
import com.mle.sbt.GenericPlugin
import com.typesafe.sbt.SbtNativePackager.Linux

/**
 * Using Linux configuration for Unix. Shame on me.
 * @author mle
 */
object UnixPlugin {
  val unixSettings: Seq[Setting[_]] = GenericPlugin.genericSettings ++ Seq(
    pkgHome in Linux := pkgHome.value / "unix",
    scriptPath := Some((pkgHome in Linux).value / scriptDir),
    scriptFiles := filesIn(scriptPath).value
  )
}
