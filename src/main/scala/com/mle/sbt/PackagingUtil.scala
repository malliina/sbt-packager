package com.mle.sbt

import sbt.{TaskKey, Task, Project, SettingKey}
import java.nio.file.{StandardCopyOption, Files, Path}
import sbt.Keys._
import com.mle.util.FileUtilities
import com.mle.sbt.GenericKeys._
import unix.UnixZipKeys
import java.io.FileNotFoundException

/**
 *
 * @author mle
 */
object PackagingUtil {
  def filesIn(dir: SettingKey[Option[Path]]): Project.Initialize[Task[Seq[Path]]] =
    (dir, name).map((path: Option[Path], pkgName) => {
      path.map(p =>
        if (Files isDirectory p) FileUtilities.listPaths(p)
        else Seq.empty[Path]
      ).getOrElse(Seq.empty[Path])
    })

  def copyTask(files: TaskKey[Seq[Path]]) = (
    basePath,
    files,
    UnixZipKeys.distribDir
    ) map ((base, filez, dest) => FileUtilities.copy(base, filez.toSet, dest).toSeq)

  def launcher(appDir: Path,
               files: Seq[Path],
               appName: String,
               extension: String,
               appFiles: Seq[Path]) = {
    val launcherFilename = appName.toLowerCase + extension
    val launcherDestination = appDir resolve launcherFilename
    val maybeLauncherFile = files.find(_.getFileName.toString == launcherFilename)
    if (maybeLauncherFile.isDefined) {
      Files.copy(maybeLauncherFile.get, launcherDestination, StandardCopyOption.REPLACE_EXISTING)
      println("Launcher: " + launcherDestination)
    } else {
      println("Did not find: " + launcherFilename)
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
      val messagesCombined = errors.mkString("\n")
      throw new FileNotFoundException(messagesCombined)
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
