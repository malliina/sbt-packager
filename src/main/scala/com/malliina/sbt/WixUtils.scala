package com.malliina.sbt

import java.io.File
import java.nio.file.Path

import com.malliina.util.Log

import scala.xml.NodeSeq

object WixUtils extends Log {
  def wixify(srcPath: String, destFileName: String): WixCompInfo = {
    val fileId = destFileName.replace('-', '_')
    val compId = "comp_" + fileId
    val comp = (<Component Id={compId} Guid='*'>
      <File Id={fileId} Name={destFileName} DiskId='1' Source={srcPath}/>
      <CreateFolder/>
    </Component>)
    val ref = (<ComponentRef Id={compId}/>)
    WixCompInfo(ref, comp)
  }

  def wixify(srcPath: String): WixCompInfo =
    wixify(srcPath, srcPath)

  def wixify(file: Path, destFileName: String): WixCompInfo = {
    val srcPath = file.toAbsolutePath.toString
    wixify(srcPath, destFileName)
  }

  def wixify(file: Path, dest: Path): WixCompInfo =
    wixify(file, dest.getFileName.toString)

  def wixify(file: Path): WixCompInfo =
    wixify(file, file.getFileName.toString)

  /**
   * Generates appropriate WIX fragments for the specified mappings.
   *
   * @param mappings file mappings (source, destination)
   * @return WIX XML fragments to use in WIX packaging
   */
  def wix(mappings: Seq[(Path, Path)]) = {
    val trees = treeify(mappings)
    add(trees.map(wixifyTree))
  }

  def wixifyTree[T](tree: Tree): WixCompInfo = tree match {
    case DirNode(d, children) =>
      val childWixInfo = children.map(wixifyTree)
      // sum of xml
      val childCompRefs = childWixInfo.map(_.ref).foldLeft(NodeSeq.Empty)(_ ++ _)
      val childComponents = childWixInfo.map(_.comp).foldLeft(NodeSeq.Empty)(_ ++ _)
      val dirId = d.toString.replace(File.separator, "_") + "_dir"
      val comp =
        (<Directory Id={dirId}
                    Name={d.getFileName.toString}>
          {childComponents}
        </Directory>)
      WixCompInfo(childCompRefs, comp)
    case PathLeaf(mapping: (Path, Path)) =>
      val (source, dest) = mapping
      wixify(source, dest)
    case anythingElse =>
      log warn "Unknown tree component: " + anythingElse
      WixCompInfo(NodeSeq.Empty, NodeSeq.Empty)
  }

  def addXml(xml: Seq[NodeSeq]) = xml.foldLeft(NodeSeq.Empty)(_ ++ _)

  def add(wixFiles: Seq[WixCompInfo]): WixCompInfo = {
    val comps = addXml(wixFiles.map(_.comp))
    val compRefs = addXml(wixFiles.map(_.ref))
    WixCompInfo(compRefs, comps)
  }

  /**
   * Returns the path that contains the <code>level</code> first names of the supplied path.
   *
   * If level is 0, returns the first name.
   *
   * If level+1 is greater than the total name count, returns None.
   *
   * @param path the original path
   * @param level the number of names to return from the path, starting at the first name
   * @return the path consisting of the names from path.getRoot to path.getName(level)
   */
  def ancestorOf(path: Path, level: Int) = {
    val endExclusive = level + 1
    if (path.getNameCount > endExclusive) {
      Some(path subpath(0, endExclusive))
    } else {
      None
    }
  }

  /**
   * Generates a tree of a sequence of items with a path.
   * Non-leaf nodes are directories and leaves are files.
   *
   * @param items sequence of files and directories
   * @param pathFunc resolves a path from a given item
   * @param level depth?
   * @tparam T
   * @return a tree based on the directory structure of the supplied items
   */
  def treeify[T](items: Seq[T], pathFunc: T => Path, level: Int = 0): Seq[Tree] = {
    val fileMap = items.groupBy(item => {
      val path = pathFunc(item)
      ancestorOf(path, level)
    })
    val leaves = fileMap.get(None)
      .map(files => files.map(file => PathLeaf(file)))
      .getOrElse(Seq.empty[PathLeaf[T]])
    val dirs = fileMap
      .filter(kv => kv._1.isDefined)
      .map(kv => DirNode(kv._1.get, treeify(kv._2, pathFunc, level + 1)))
      .toSeq
    leaves ++ dirs
  }

  def treeify(mappings: Seq[(Path, Path)]) = treeify[(Path, Path)](mappings, pair => pair._2)
}

abstract class Tree

case class PathLeaf[T](value: T) extends Tree

case class DirNode(name: Path, children: Seq[Tree]) extends Tree

case class WixCompInfo(ref: NodeSeq, comp: NodeSeq)
