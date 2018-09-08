package com.malliina.sbt

import java.io.{BufferedWriter, FileNotFoundException, FileWriter, PrintWriter}
import java.nio.file.{Files, Path, StandardCopyOption}

import com.malliina.sbt.file.FileVisitors
import com.malliina.sbt.unix.UnixZipKeys
import sbt.Keys._
import sbt._

object PackagingUtil {
  val lineSep = sys.props("line.separator")

  def writerTo(path: Path)(op: PrintWriter => Unit) {
    Files.createDirectories(path.getParent)
    writeTo(path)(op)
  }

  def logPairs(pairs: Seq[(SettingKey[Path], Types.Id[Path])], logger: TaskStreams) {
    pairs.foreach { pair =>
      val (key, value) = pair
      val actualKey = key.key
      val keyPrinted = actualKey.description.getOrElse(actualKey.label)
      logger.log.info(keyPrinted + "\n" + value.toAbsolutePath.toString + "\nExists: " + Files.exists(value))
    }
  }

  def relativize(files: Seq[Path], baseDir: Path) = files.map { file =>
    file -> (baseDir relativize file)
  }

  def filesIn(dir: SettingKey[Option[Path]]): Def.Initialize[Task[Seq[Path]]] = Def.task {
    dir.value.map { p =>
      if (Files isDirectory p) listPaths(p)
      else Seq.empty[Path]
    }.getOrElse { Seq.empty[Path] }
  }

  def listFiles(dir: SettingKey[Path]): Def.Initialize[Task[Seq[Path]]] = Def.task {
    val dirValue = dir.value
    if (Files isDirectory dirValue) listPaths(dirValue)
    else Seq.empty[Path]
  }

  def copyTask(files: TaskKey[Seq[Path]]) = Def.task {
    copy(GenericKeys.basePath.value, files.value.toSet, UnixZipKeys.distribDir.value).toSeq
  }

  def launcher(appDir: Path,
               files: Seq[Path],
               appName: String,
               extension: String,
               appFiles: Seq[Path],
               logger: TaskStreams) = {
    val launcherFilename = appName.toLowerCase + extension
    val launcherDestination = appDir resolve launcherFilename
    val maybeLauncherFile = files.find(_.getFileName.toString == launcherFilename)
    val msg =  if (maybeLauncherFile.isDefined) {
      Files.copy(maybeLauncherFile.get, launcherDestination, StandardCopyOption.REPLACE_EXISTING)
      "Launcher: " + launcherDestination
    } else {
      "Did not find: " + launcherFilename
    }
    logger.log info msg
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
      val messagesCombined = errors mkString lineSep
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

  /**
    * Copies the given files to a destination directory, where the files' subdirectory is calculated relative to the given base directory.
    *
    * @param srcBase the base directory for the source files
    * @param files   the source files to copy
    * @param dest    the destination directory, so each source file is copied to <code>dest / srcBase.relativize(file)</code>
    * @return the destination files
    */
  def copy(srcBase: Path, files: Set[Path], dest: Path) = files map (file => {
    val destFile = rebase(file, srcBase, dest)
    // Create parent dirs if they don't exist
    val parentDir = destFile.getParent
    if (parentDir != null && !Files.isDirectory(parentDir))
      Files createDirectories parentDir
    // Target directory guaranteed to exist, so copy the target, unless it is a directory that already exists
    if (!Files.isDirectory(destFile))
      Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING)
    else destFile
  })

  def rebase(file: Path, srcBase: Path, destBase: Path) = destBase resolve (srcBase relativize file)

  /**
    * Performs a recursive search of files and directories under the given base path.
    *
    * @param basePath the base directory
    * @return The files and directories under the base directory. Directories precede any files they contain in the returned sequence.
    */
  def listPaths(basePath: Path): Seq[Path] = {
    val visitor = new FileVisitors.FileAndDirCollector
    Files walkFileTree(basePath, visitor)
    visitor.files
  }

  def writeTo(filename: Path)(op: PrintWriter => Unit): Unit =
    using(new PrintWriter(new BufferedWriter(new FileWriter(filename.toFile))))(op)

  def using[T <: AutoCloseable, U](resource: T)(op: T => U): U =
    try {
      op(resource)
    } finally {
      resource.close()
    }
}
