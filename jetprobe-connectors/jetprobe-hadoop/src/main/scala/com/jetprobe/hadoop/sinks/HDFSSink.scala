package com.jetprobe.hadoop.sinks

import java.io.File

import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.sink.DataSink
import com.jetprobe.hadoop.actions._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

/**
  * @author Shad.
  */
class HDFSSink(conf : Configuration) extends DataSink{


  def copy(localPath : String, hdfsDir : String) : ActionBuilder = new HDFSActionBuilder(conf,CopyToHDFS(new File(localPath),new Path(hdfsDir)))
  def delete(path : String) : ActionBuilder = new HDFSActionBuilder(conf,DeletePath(new Path(path)))
  def mkdir(path : String) : ActionBuilder = new HDFSActionBuilder(conf,MkDir(new Path(path)))
  def download(from : String,to : String) = new HDFSActionBuilder(conf,DownloadFile(new Path(from),new File(to)))

}

object HDFSSink {

  def apply(uri : String) : HDFSSink = {
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)
    conf.setBoolean("dfs.client.use.datanode.hostname",false)
    new HDFSSink(conf)
  }

}