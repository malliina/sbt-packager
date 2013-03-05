package com.mle.sbt

import xml.NodeSeq
import java.io.File
import java.nio.file.Path
import com.mle.util.Log

/**
 * @author Michael
 */
object WixUtils extends Log {
  /**
   * Generates appropriate WIX fragments for the specified mappings.
   * @param mappings file mappings (source, destination)
   * @return WIX XML framents to use in WIX packaging
   */
  def wix(mappings: Seq[(Path, Path)]) = {
     val trees = treeify(mappings)
     add(trees.map(wixify))
  }

  def wixify[T](tree: Tree): WixCompInfo = tree match {
    case DirNode(d, children) =>
      val childWixInfo = children.map(wixify)
      // sum of xml
      val childCompRefs = childWixInfo.map(_.compRefs).foldLeft(NodeSeq.Empty)(_ ++ _)
      val childComponents = childWixInfo.map(_.compElems).foldLeft(NodeSeq.Empty)(_ ++ _)
      val dirId = d.toString.replace(File.separator, "_") + "_dir"
      val comp =
        (<Directory Id={dirId} Name={d.getFileName.toString}>
          {childComponents}
        </Directory>)
      WixCompInfo(childCompRefs, comp)
    case PathLeaf(mapping: (Path, Path)) =>
      val (source, dest) = mapping
      val fileName = dest.getFileName.toString
      val fileId = fileName.replace('-', '_')
      val compId = fileId + "_comp"
      val comp = (<Component Id={compId} Guid="*">
        <File Id={fileId} Name={fileName} DiskId="1" Source={source.toAbsolutePath.toString}/>
        <CreateFolder/>
      </Component>)
      val compIdXml = (<ComponentRef Id={compId}/>)
      WixCompInfo(compIdXml, comp)
    case anythingElse =>
      log warn "Unknown tree component: " + anythingElse
      WixCompInfo(NodeSeq.Empty, NodeSeq.Empty)
  }

  def addXml(xml: Seq[NodeSeq]) = xml.foldLeft(NodeSeq.Empty)(_ ++ _)

  def add(wixFiles: Seq[WixCompInfo]): WixCompInfo = {
    val comps = addXml(wixFiles.map(_.compElems))
    val compRefs = addXml(wixFiles.map(_.compRefs))
    WixCompInfo(compRefs, comps)
  }

  /**
   * Returns the path that contains the <code>level</code> first names of the supplied path.
   *
   * If level is 0, the returned path is the root.
   *
   * If level+1 is greater than the total name count, returns None.
   *
   * @param path
   * @param level
   * @return the path consisting of the names from path.getRoot to path.getName(level)
   */
  def ancestorOf(path: Path, level: Int) = {
    val ancestors = path.getNameCount
    val stepsUp = ancestors - (level + 1)
    if (stepsUp > 0) {
      var eventualParent = path
      (0 until stepsUp) map (i => {
        eventualParent = eventualParent.getParent
      })
      Some(eventualParent)
    } else {
      None
    }
  }

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

case class WixCompInfo(compRefs: NodeSeq, compElems: NodeSeq)
