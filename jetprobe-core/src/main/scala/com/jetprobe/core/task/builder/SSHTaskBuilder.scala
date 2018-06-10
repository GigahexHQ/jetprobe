package com.jetprobe.core.task.builder

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.Scanner

import akka.actor.ActorSystem
import com.jetprobe.core.parser.ExpressionParser
import com.jetprobe.core.task._
import com.jetprobe.core.structure.PipelineContext
import com.jetprobe.core.task.builder.SSHTaskBuilder._
import com.typesafe.scalalogging.LazyLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile

import scala.collection.mutable


/**
  * @author Shad.
  */

case object SSHTask extends TaskType

class SSHTaskBuilder(val description: String, fnShellTask: SecuredClient => Unit, sSHConfig: SSHConfig)
  extends TaskBuilder with LazyLogging {

  val taskMeta = TaskMeta(description, SSHTask)

  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */

  override def build(ctx: PipelineContext, next: ExecutableTask): ExecutableTask = {
    val sshMessage = SSHMessage(fnShellTask, sSHConfig, next)


    new SelfExecutableTask(taskMeta, sshMessage, next, ctx.system, ctx.controller)({
      case (message, sess) => message match {
        case SSHMessage(fnTask, config, next) =>

          val sshClient = new SSHClient()
          sshClient.addHostKeyVerifier(new PromiscuousVerifier)
          sshClient.loadKnownHosts()
          sshClient.connect(config.hostName)
          sshClient.setConnectTimeout(5)
          sshClient.authPassword(config.user, config.password)
          try {
            val sc = new SecuredClient(sshClient, ctx.system)
            fnTask(sc)
            val taskQueue = sc.remoteTasks
            var attributes = sess.attributes
            var updatedSession = sess
            while (taskQueue.nonEmpty) {
              val taskAttr = taskQueue.dequeue().exec(updatedSession.attributes)
              attributes = taskAttr ++ attributes
              updatedSession = sess.copy(attributes = attributes)
            }
            updatedSession
          } finally sshClient.disconnect

      }
    }
    )
  }


}

object SSHTaskBuilder {
  type HandleOp = String => Map[String, Any]

  import ExpressionParser._

  sealed trait SSHTaskDef extends LazyLogging {


    private[task] def exec(config: Map[String, Any]): Map[String, Any]

  }

  val defaultHandler: HandleOp = (s: String) => {
    println(s)
    Map.empty
  }

  /**
    * Remote Command Task builder
    *
    * @param cmd
    * @param streamHandler
    * @param ssh
    */
  class RemoteExec(cmd: String, var streamHandler: HandleOp = defaultHandler, ssh: SSHClient) extends SSHTaskDef {

    def consume(handler: HandleOp) = streamHandler = handler

    override private[task] def exec(config: Map[String, Any]): Map[String, Any] = {
      ExpressionParser.parse(cmd, config) match {
        case Some(cmdVal) => executeCmd(cmdVal, streamHandler)
      }
    }

    private def executeCmd(cmd: String, streamHandler: HandleOp): Map[String, Any] = {
      val session = ssh.startSession
      try {
        val strippedCmdStr = cmd.stripMargin.replaceAll("\n", " ").replaceAll("\r", "")
        val run = session.exec(strippedCmdStr)

        val errorStream = new StreamLog(run.getErrorStream, streamHandler)
        val infoStream = new StreamLog(run.getInputStream, streamHandler)
        errorStream.start()
        infoStream.start()

        errorStream.join(Long.MaxValue)
        infoStream.join(Long.MaxValue)
        logger.info(s"exit status : ${run.getExitStatus}")

        if (run.getExitStatus != 0) {
          throw new Exception(s"cmd : ${strippedCmdStr} failed.")
        }

        errorStream.properties ++ infoStream.properties

      } finally session.close
    }
  }

  case class UploadTask(from: String, remotePath: String, ssh: SSHClient) extends SSHTaskDef {

    override private[task] def exec(config: Map[String, Any]): Map[String, Any] = {
      (parse(from, config), parse(remotePath, config)) match {
        case (Some(fromPath), Some(toPath)) =>
          try {
            ssh.newSCPFileTransfer.upload(new FileSystemFile(fromPath), toPath)
            Map.empty
          }
          catch {
            case e: Exception =>
              logger.error(s"Exception occurred while uploading task : ${e.getMessage}")
              throw e
          }

        case _ => throw new Exception(s"Unable to parse the input : ${from} => ${remotePath}")
      }
    }

  }

  case class DownloadTask(from: String, target: String, ssh: SSHClient) extends SSHTaskDef {

    override private[task] def exec(config: Map[String, Any]): Map[String, Any] = {

      (parse(from, config), parse(target, config)) match {
        case (Some(fromPath), Some(toPath)) =>
          ssh.newSCPFileTransfer.download(fromPath, new FileSystemFile(toPath))
          Map.empty
      }

    }

  }


}


case class SSHMessage(fnShellTask: SecuredClient => Unit, sshConfig: SSHConfig, next: ExecutableTask) extends TaskMessage {

  override def name: String = s"SSH Tasks at host : ${sshConfig.hostName}"
}

object SecuredClient {

  def defaultStreamHandler: String => Any = println(_)
}

class SecuredClient(ssh: SSHClient, actorSystem: ActorSystem) extends LazyLogging {

  val remoteTasks: mutable.Queue[SSHTaskDef] = mutable.Queue.empty

  import SecuredClient._

  def run(cmd: String): RemoteExec = {
    val rt = new RemoteExec(cmd, ssh = ssh)
    remoteTasks.enqueue(rt)
    rt
  }


  def download(from: String, target: String): Unit = {

    val downloadTask = DownloadTask(from, target, ssh)
    remoteTasks.enqueue(downloadTask)

  }

  def upload(src: String, target: String): Unit = {

    val uploadTask = UploadTask(src, target, ssh)
    remoteTasks.enqueue(uploadTask)

  }

}

class StreamLog(stream: InputStream, opHandler: HandleOp) extends Thread {

  var properties: Map[String, Any] = Map.empty

  override def run(): Unit = {
    val scanner = new Scanner(stream, "UTF-8").useDelimiter("\n")
    val streamReader = new InputStreamReader(stream)
    while (scanner.hasNext) {
      val parsedProp = opHandler(scanner.next())
      if(parsedProp.nonEmpty){
        properties = properties ++ parsedProp
      }

    }
    scanner.close()
  }

}