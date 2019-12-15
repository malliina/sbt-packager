package com.malliina.sbt.mac

import sbt.Keys._

import scala.sys.process.Process

object ExeUtils {

  /** Executes the supplied command with the given parameters,
    * logging the command and any subsequent output using the logger's INFO level.
    *
    * @param cmd    command to execute
    * @param logger the logger
    */
  def execute(cmd: Seq[String], logger: TaskStreams) {
    val output = execute2(cmd, logger)
    output.foreach(line => logger.log.info(line))
  }

  /** Executes the supplied command, logging only the command executed.
    *
    * @return all output lines up to termination
    */
  def execute2(cmd: Seq[String], logger: TaskStreams): Stream[String] = {
    logger.log.info(cmd.mkString(" "))
    Process(cmd.head, cmd.tail).lineStream
  }
}
