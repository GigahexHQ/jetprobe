package com.jetprobe.core.fs

import com.jetprobe.core.storage.Storage
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult}
import java.io.{File => JFile}

import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.structure.Config
/**
  * @author Shad.
  */
class DirectoryStorage private[jetprobe](path: String) extends Storage with ValidationExecutor[DirectoryStorage]{

  val underlyingDir = new JFile(path)

  def rm(recursive : Boolean = false) : Boolean = underlyingDir.delete()

  def mkdir : Boolean = underlyingDir.mkdir()

  def hasTotalFiles(fileCount : Int) : ValidationResult = {
    assertThat[JFile,Int](fileCount,underlyingDir)(dir => dir.listFiles().count(_.isFile))
  }

  def hasTotalDirs(dirCount : Int) : ValidationResult = {
    assertThat[JFile,Int](dirCount,underlyingDir)(dir => dir.listFiles().count(_.isDirectory))
  }

  def hasFiles(fileCount : Int)(filterFn : JFile => Boolean) : ValidationResult = {
    assertThat[JFile,Int](fileCount,underlyingDir)(dir => dir.listFiles().count(f => f.isFile && filterFn.apply(f)))
  }

  def hasDirectories(dirCount : Int)(filterFn : JFile => Boolean) : ValidationResult = {
    assertThat[JFile,Int](dirCount,underlyingDir)(dir => dir.listFiles().count(f => f.isDirectory && filterFn.apply(f)))
  }


}

class DirPath(path: String) extends Config[DirectoryStorage] {

  override private[jetprobe] def getStorage(sessionConf: Map[String, Any]): DirectoryStorage = {
    ExpressionParser.parse(path, sessionConf) match {
      case Some(p) => new DirectoryStorage(p)
      case None => throw new IllegalArgumentException(s"Unable to parse the expression : ${path}")
    }
  }
}
