package com.jetprobe.hadoop.sinks

import java.io.File
import java.net.URI

import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.storage.DataSink
import com.jetprobe.hadoop.actions._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * @author Shad.
  */

class HDFSSink(val conf: Configuration, val hadoopUserName: String, val fs: FileSystem) extends DataSink {

  def getFileSystem : FileSystem = {
    init(s"/usesr/${hadoopUserName}")
    fs
  }

  private[this] def init(path : String) = {
    conf.get("fs.defaultFS")
    fs.initialize(new URI(conf.get("fs.defaultFS") + path), conf)
  }

  override def cleanup: Unit = fs.close()

  def write(records: Iterator[String], at: String): Unit = {
    init(at)
    val outputStream = fs.create(new Path(at))
    records.foreach(s => outputStream.writeBytes(s))
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
    init(path)
    fs.delete(new Path(path), true)
  }

  def moveFromLocal(localSrc : String,destination : String) : Unit = {
    init(destination)
    fs.copyFromLocalFile(true,new Path(localSrc),new Path(destination))
  }

  def copyFromLocal(localSrc : String,destination : String) : Unit = {
    init(destination)
    fs.copyFromLocalFile(false,new Path(localSrc),new Path(destination))
  }



  def copy(localPath: String, hdfsDir: String): ActionBuilder = new HDFSActionBuilder(conf, new CopyToHDFS(new File(localPath), new Path(hdfsDir)), hadoopUserName)

  def delete(path: String): ActionBuilder = new HDFSActionBuilder(conf, new DeletePath(new Path(path)), hadoopUserName)

  //def mkdir(path: String): ActionBuilder = new HDFSActionBuilder(conf, new MkDir(new Path(path)),hadoopUserName)

  def download(from: String, to: String): ActionBuilder = new HDFSActionBuilder(conf, new DownloadFile(new Path(from), new File(to)), hadoopUserName)

}

object HDFSSink {

  def apply(uri: String, loginAs: String = "root"): HDFSSink = {
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)
    conf.setBoolean("dfs.client.use.datanode.hostname", false)
    System.setProperty("HADOOP_USER_NAME", loginAs)
    new HDFSSink(conf, loginAs, FileSystem.get(conf))
  }

}