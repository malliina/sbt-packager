package com.mle.sbt

import java.io.{FileNotFoundException, PrintWriter}
import java.nio.file.{Files, Path, StandardCopyOption}

import com.mle.file.FileUtilities
import com.mle.sbt.GenericKeys._
import com.mle.sbt.unix.UnixZipKeys
import sbt.Keys._
import sbt._

/**
 *
 * @author mle
 */
object PackagingUtil {
  def writerTo(path: Path)(op: PrintWriter => Unit) {
    Files.createDirectories(path.getParent)
    FileUtilities.writerTo(path)(op)
  }

  def logPairs(pairs: Seq[(SettingKey[Path], Types.Id[Path])], logger: TaskStreams) {
    pairs.foreach(pair => {
      val (key, value) = pair
      val actualKey = key.key
      val keyPrinted = actualKey.description.getOrElse(actualKey.label)
      logger.log.info(keyPrinted + "\n" + value.toAbsolutePath.toString + "\nExists: " + Files.exists(value))
    })
  }

  def relativize(files: Seq[Path], baseDir: Path) = files.map(file => {
    file -> (baseDir relativize file)
  })

  def filesIn(dir: SettingKey[Option[Path]]): Def.Initialize[Task[Seq[Path]]] = Def.task {
    dir.value.map(p =>
      if (Files isDirectory p) FileUtilities.listPaths(p)
      else Seq.empty[Path]
    ).getOrElse(Seq.empty[Path])
  }

  def listFiles(dir: SettingKey[Path]): Def.Initialize[Task[Seq[Path]]] = Def.task {
    val dirValue = dir.value
    if (Files isDirectory dirValue) FileUtilities.listPaths(dirValue)
    else Seq.empty[Path]
  }

  def copyTask(files: TaskKey[Seq[Path]]) = (
    basePath,
    files,
    UnixZipKeys.distribDir
    ) map ((base, filez, dest) => FileUtilities.copy(base, filez.toSet, dest).toSeq)

  def launcher(appDir: Path,
               files: Seq[Path],
               appName: String,
               extension: String,
               appFiles: Seq[Path],
               logger: TaskStreams) = {
    val launcherFilename = appName.toLowerCase + extension
    val launcherDestination = appDir resolve launcherFilename
    val maybeLauncherFile = files.find(_.getFileName.toString == launcherFilename)
    if (maybeLauncherFile.isDefined) {
      Files.copy(maybeLauncherFile.get, launcherDestination, StandardCopyOption.REPLACE_EXISTING)
      logger.log.info("Launcher: " + launcherDestination)
    } else {
      logger.log.info("Did not find: " + launcherFilename)
    }
    appFiles
  }

  /**
   * Removes section from name
   */
  def stripSection(name: String, section: String) =
    if (name.contains(section) && name.endsWith(".jar"))
      name.slice(0, name indexOf section) + ".jar"
    else
      name

  def verifyPathSetting(settings: (SettingKey[Path], Path)*) {
    val errors = settings flatMap verifyPath
    if (errors.nonEmpty) {
      val messagesCombined = errors mkString FileUtilities.lineSep
      throw new FileNotFoundException("The following files were not found: \n" + messagesCombined)
    }
  }

  private def verifyPath(setting: (SettingKey[Path], Path)): Option[String] = {
    val (s, path) = setting
    if (!Files.exists(path)) {
      val desc = s.key.description.map(": " + _).getOrElse("")
      Some("Not found: " + path.toAbsolutePath.toString + ", please configure " + s.key.label + desc)
    } else {
      None
    }
  }
}
