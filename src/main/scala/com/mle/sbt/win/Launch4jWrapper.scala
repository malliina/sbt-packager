package com.mle.sbt.win

import com.mle.util.FileUtilities
import java.nio.file.Path
import sys.process.Process
import xml.NodeSeq

object Launch4jWrapper {
  /**
   * Creates a .exe file that wraps the given jar and starts the main class when opened.
   *
   * Launch4j is used to wrap the jar.
   *
   * @param launch4jcExe path to launch4jc.exe executable
   * @param jarFile the .jar to wrap
   * @param mainClass the main class to start when the .exe is started
   * @param appJarName the name of the app jar when deployed
   * @param exe the destination exe to create
   * @return the created exe
   */
  def exeWrapper(launch4jcExe: Path,
                 jarFile: Path,
                 mainClass: String,
                 appJarName: String,
                 outputConf: Path,
                 exe: Path,
                 icon: Path) = {
    val config = launcherConfig(jarFile, mainClass, appJarName, exe, icon)
    buildLauncher(launch4jcExe, config, outputConf, exe)
  }

  def buildLauncher(launch4jcExe: Path, config: NodeSeq, outputConf: Path, outputExe: Path) = {
    FileUtilities.writerTo(outputConf)(_.println(config.toString()))
    println("Executing: " + launch4jcExe.toAbsolutePath + " " + outputConf.toAbsolutePath.toString)
    Process(launch4jcExe.toAbsolutePath.toString, Seq(outputConf.toAbsolutePath.toString)).! match {
      case 0 => () // success
      case errorValue => throw new Exception("Unable to create .exe wrapper: "
        + outputExe.toAbsolutePath + ". Launch4j exit value: " + errorValue)
    }
    outputExe
  }

  /**
   *
   * @param exe the destination exe, created when this config is supplied to launch4jc.exe
   * @return the launch4j XML config
   */
  def launcherConfig(jarFile: Path, mainClass: String, appJarName: String, exe: Path, icon: Path) = {
    //    require(Files.exists(jarFile), "Not found: " + jarFile.toAbsolutePath)
    //    val jarFileName = jarFile.getFileName.toString
    (<launch4jConfig>
      <dontWrapJar>true</dontWrapJar>
      <headerType>gui</headerType>
      <jar>{appJarName}</jar>
      <outfile>{exe.getFileName.toString}</outfile>
      <errTitle></errTitle>
      <cmdLine></cmdLine>
      <chdir></chdir>
      <priority>normal</priority>
      <downloadUrl>http://java.com/download</downloadUrl>
      <supportUrl></supportUrl>
      <customProcName>false</customProcName>
      <stayAlive>false</stayAlive>
      <manifest></manifest>
      <icon>{icon.toAbsolutePath.toString}</icon>
      <singleInstance>
        <mutexName>{appJarName}</mutexName>
        <windowTitle></windowTitle>
      </singleInstance>
      <classPath>
        <mainClass>{mainClass}</mainClass>
        <cp>{appJarName};lib/*</cp>
      </classPath>
      <jre>
        <path></path>
        <minVersion>1.7.0</minVersion>
        <maxVersion></maxVersion>
        <jdkPreference>preferJre</jdkPreference>
      </jre>
    </launch4jConfig>)
  }
}