package com.jetprobe.hadoop.storage

import java.net.URI

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.Config
import com.jetprobe.core.validations.ValidationExecutor
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}

/**
  * @author Shad.
  */

class HDFSStorage private[jetprobe](val conf: Configuration, val hadoopUserName: String, val fs: FileSystem)
  extends Storage{

  implicit def toPath(path : String) : Path = new Path(path)


  val batch = 10000
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
    records.grouped(batch).foreach { records =>
      records.foreach(s => outputStream.writeBytes(s + "\n"))
      outputStream.flush()
    }
    outputStream.flush()
    outputStream.close()
    fs.close()

  }

  def readLines(path : String) : Iterator[String] = {
    init(path)
    val input = fs.open(path)
    IOUtils.toString(input).split("\n").toIterator
  }

  def file(path : String) : HDFSPath = HDFSPath(path,this)


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