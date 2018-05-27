package com.jetprobe.core.fs

import java.io._
import java.nio.channels.FileChannel

import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.storage.{Storage, StorageQuery}
import com.jetprobe.core.structure.Config
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}

import scala.io.Source
import java.io.{File => JFile}


/**
  * @author Shad.
  */
class FileStorage private[jetprobe](path: String) extends Storage with ValidationExecutor[FileStorage] {

  val underLyingFile = new JFile(path)

  /**
    * Move the file to the destination
    *
    * @param destination
    */
  def moveTo(destination: String): Unit = {
    new File(path).renameTo(new File(destination))
  }

  /**
    * Write the data to the file
    *
    * @param data
    */
  def write(data: Iterator[String]): Unit = {
    val f = new File(path)

    if (!f.exists()) {
      f.createNewFile()
    }
    val writer = new BufferedWriter(new FileWriter(f))
    data.foreach { s =>
      writer.write(s)
    }
    writer.flush()
    writer.close()
  }

  /**
    * Copy the file to the destination
    *
    * @param destination
    */
  def copyTo(destination: String): Unit = {
    var sourceChannel: FileChannel = null
    var destChannel: FileChannel = null
    try {
      sourceChannel = new FileInputStream(new JFile(path)).getChannel
      destChannel = new FileOutputStream(new File(destination)).getChannel
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size)
    } finally {
      sourceChannel.close()
      destChannel.close()
    }
  }

  def usingFile[T](fn: File => T): T = {
    fn(underLyingFile)
  }

  def lines(): Iterator[String] = {
    Source.fromFile(new JFile(path)).getLines()
  }


  /**
    *
    * @param count
    * @return
    */
  def hasTotalLines(count: Int): ValidationResult = {
    val file = new File(path)
    assertThat[File, Int](count, file) { file =>
      Source.fromFile(file).getLines().size
    }
  }

  /**
    * Assert if the file is nonEmpty
    *
    * @return
    */
  def isNonEmpty(): ValidationResult = {
    val file = new File(path)
    assertThat[File, Boolean](true, file) { file =>
      Source.fromFile(file).getLines().nonEmpty
    }
  }

  def isDirectory() : ValidationResult = {
    assertThat(true,underLyingFile)(file => file.isDirectory)
  }


  /**
    * assert count of lines based on some condition
    * @param count
    * @param whereCondn
    * @return
    */
  def hasLines(count: Int)(whereCondn: String => Boolean): ValidationResult = {
    assertThat(count,underLyingFile){ file =>
      Source.fromFile(file).getLines().count(whereCondn)
    }
  }


  def hasLines(whereCondn: String => Boolean): ValidationResult = {
    assertThat(true,underLyingFile){ file =>
      Source.fromFile(file).getLines().count(whereCondn) > 0
    }
  }




  /**
    * Delete the file
    *
    * @return true if success else false
    */
  def rm: Boolean = new File(path).delete()


}

class FilePath(path: String) extends Config[FileStorage] {

  override private[jetprobe] def getStorage(sessionConf: Map[String, Any]): FileStorage = {
    ExpressionParser.parse(path, sessionConf) match {
      case Some(p) => new FileStorage(p)
      case None => throw new IllegalArgumentException(s"Unable to parse the expression : ${path}")
    }
  }
}


