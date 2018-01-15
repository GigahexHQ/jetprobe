package com.jetprobe.hadoop.storage

import java.io.File
import java.net.URI

import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.storage.{Storage, StorageQuery}
import com.jetprobe.core.structure.Config
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.hadoop.actions._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}

/**
  * @author Shad.
  */

class HDFSStorage private[jetprobe](val conf: Configuration, val hadoopUserName: String, val fs: FileSystem) extends Storage {

  def getFileSystem: FileSystem = {
    init(s"/usesr/${hadoopUserName}")
    fs
  }

  private[this] def init(path: String) = {
    conf.get("fs.defaultFS")
    fs.initialize(new URI(conf.get("fs.defaultFS") + path), conf)
  }

  override def cleanup: Unit = fs.close()

  def write(records: Iterator[String], at: String): Unit = {
    init(at)
    val outputStream = fs.create(new Path(at))
    records.foreach(s => outputStream.writeBytes(s + "\n"))
    outputStream.close()

  }

  /**
    * Directory path to create
    *
    * @param path
    */
  def mkdir(path: String): Unit = {
    init(path)
    fs.mkdirs(new Path(path))
  }

  /**
    * HDFS Path to delete
    *
    * @param path
    */
  def rm(path: String): Boolean = {
    init(path)
    fs.delete(new Path(path), false)
  }

  /**
    * Path to delete recurisvely
    *
    * @param path
    */
  def rmr(path: String): Boolean = {
    usingFS(fs => fs.delete(new Path(path), true)) match {
      case Some(x) => x
      case None => false
    }
  }



  def moveFromLocal(localSrc: String, destination: String): Unit = {
    usingFS(fs => fs.copyFromLocalFile(true, new Path(localSrc), new Path(destination)))
  }

  def copyFromLocal(localSrc: String, destination: String): Unit = {
    usingFS(fs => fs.copyFromLocalFile(false, new Path(localSrc), new Path(destination)))
  }

  def getFileStatus(path : String) : Option[ContentSummary] = {
    usingFS { fs =>
      fs.getContentSummary(new Path(path))
    }
  }

  def copyToLocal(src : String,destination : String) : Unit = {
    usingFS(fs => fs.copyToLocalFile(new Path(src),new Path(destination)))
  }



 def usingFS[T](fn : FileSystem => T) : Option[T] = {

    val fs = FileSystem.get(conf)
    try {
      Some(fn(fs))
    } catch {
      case e : Exception =>
        logger.error(e.getMessage)
        None
    }
    finally fs.close()

  }

}

class HDFSConfig(uri: String, loginAs: String) extends Config[HDFSStorage] {

  override private[jetprobe] def getStorage(config: Map[String,Any]) = {
    ExpressionParser.parseAll(Seq(Expr(uri),Expr(loginAs)),config) match {

      case Left(exception) => throw exception
      case Right(parsedMap) =>
        val conf = new Configuration()
        conf.set("fs.defaultFS", parsedMap(uri))
        conf.setBoolean("dfs.client.use.datanode.hostname", false)
        System.setProperty("HADOOP_USER_NAME", parsedMap(loginAs))
        new HDFSStorage(conf, parsedMap(loginAs), FileSystem.get(conf))

    }

  }

}