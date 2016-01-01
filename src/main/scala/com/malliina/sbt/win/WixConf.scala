package com.malliina.sbt.win

import java.nio.file.Path

/**
 * wip
 * @author mle
 */
trait WixConf {
  def appName: String

  def jarName: String

  def libz: Seq[Path]

  def exe: Path

  def bat: Path

  /**
   *
   * @return license.rtf, the content of which is displayed prior to installation
   */
  def license: Path

  /**
   * @return icon for MSI installation package file
   */
  def icon: Path

  /**
   * @return path to the original winsw.exe
   */
  def winswExe: Path

  /**
   * @return name of winsw.exe on the target
   */
  def winswExeName: String

  /**
   * @return name of winsw.xml on the target
   */
  def winswConfName: String

  def homeVar: String
}
