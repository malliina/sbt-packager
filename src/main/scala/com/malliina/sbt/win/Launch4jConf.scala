package com.malliina.sbt.win

import java.nio.file.Path

trait Launch4jConf {

  /**
    * @return path to launch4jc.exe
    */
  def launch4jcExe: Path

  /**
    * @return path to the main jar file which the created exe will launch
    */
  def jarFile: Path

  /**
    * @return the main class to start when the exe is run
    */
  def mainClass: String

  /**
    * @return the name of the app jar when deployed
    */
  def appJarName: String

  /** This file will be created prior to executing launch4jc.exe.
    *
    * @return XML configuration file path to pass to launch4jc.exe
    */
  def outputConf: Path

  /**
    * @return the exe to create when launch4jc.exe is run
    */
  def exe: Path

  /**
    * @return the icon to put on the exe
    */
  def icon: Path
}
