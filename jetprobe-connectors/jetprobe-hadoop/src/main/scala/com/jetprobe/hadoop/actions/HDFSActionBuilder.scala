package com.jetprobe.hadoop.actions

import java.io.File

import com.jetprobe.core.action.{Action, ActionMessage, SelfExecutableAction}
import com.jetprobe.core.action.builder.ActionBuilder
import com.jetprobe.core.structure.ScenarioContext
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}

/**
  * @author Shad.
  */
class HDFSActionBuilder(conf : Configuration, hdfsAction : HDFSActionMessage) extends ActionBuilder{

  lazy val fs : FileSystem = FileSystem.get(conf)
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {

    System.setProperty("HADOOP_USER_NAME", "root")
    /*try{
      fs =
     } catch {
      case ex : Exception =>
        logger.warn(ex.getMessage)
    }*/

    new SelfExecutableAction("HDFSAction",hdfsAction,next,ctx.system,ctx.controller)(
      (message,session) => {
        message match {
          case m : CopyToHDFS =>
            logger.info(s"Copying the file : ${m.toHDFS.getName}")
            m.execute(fs,conf)

          case m :DeletePath =>
            logger.info("Downloading the file")
            m.execute(fs)

          case m :MkDir =>
            logger.info(s"Creating new directory : ${m.path.getName}")
            m.execute(fs)
          case m : DownloadFile =>
            logger.info(s"Downloading the file : ${m.src.getName}")
            m.execute(fs)
        }
        session
      }
    )
  }

}

trait HDFSActionMessage extends ActionMessage {

  override def name: String = this.toString

}

case class CopyToHDFS(fromLocal : File, toHDFS : Path) extends HDFSActionMessage {

  def execute(fs : FileSystem,conf : Configuration) : Unit = {
    FileUtil.copy(fromLocal,fs,toHDFS,true,conf)
  }

}

case class MkDir(path : Path) extends HDFSActionMessage {

  def execute(fs : FileSystem) : Unit = {
    fs.mkdirs(path)
  }

}

case class DeletePath(path : Path) extends HDFSActionMessage {

  def execute(fs : FileSystem) : Unit = {
    fs.delete(path,true)
  }

}

case class DownloadFile(src : Path,dst : File) extends HDFSActionMessage {

  def execute(fs : FileSystem) : Unit = {

    fs.copyToLocalFile(src,new Path(dst.getAbsolutePath))
  }

}