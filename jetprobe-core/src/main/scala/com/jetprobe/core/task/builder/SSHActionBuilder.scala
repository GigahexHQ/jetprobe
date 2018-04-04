package com.jetprobe.core.task.builder

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.Scanner

import akka.actor.ActorSystem
import com.jetprobe.core.task._
import com.jetprobe.core.structure.PipelineContext
import com.typesafe.scalalogging.LazyLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile


/**
  * @author Shad.
  */
class SSHTaskBuilder(fnShellTask: SecuredClient => Unit, sSHConfig: SSHConfig) extends TaskBuilder with LazyLogging {

  private val name = "Secured-Shell-Task"

  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */

  override def build(ctx: PipelineContext, next: Task): Task = {
    val sshMessage = SSHMessage(fnShellTask, sSHConfig, next)


    new SelfExecutableTask(name, sshMessage, next, ctx.system, ctx.controller)({
      case (message, sess) => message match {
        case SSHMessage(fnTask, config, next) =>

          val sshClient = new SSHClient()
          sshClient.addHostKeyVerifier(new PromiscuousVerifier)
          sshClient.loadKnownHosts()
          sshClient.connect(config.hostName)
          sshClient.setConnectTimeout(5)
          sshClient.authPassword(config.user, config.password)
          try {
            fnTask(new SecuredClient(sshClient,ctx.system))
          } finally sshClient.disconnect

          //return the same session
          sess

      }
    }
    )
  }


}

case class SSHMessage(fnShellTask: SecuredClient => Unit, sshConfig: SSHConfig, next: Task) extends TaskMessage {

  override def name: String = s"SSH Tasks at host : ${sshConfig.hostName}"
}

class SecuredClient(ssh: SSHClient,actorSystem: ActorSystem) extends LazyLogging {


  def run(cmd: String): Unit = {

    val session = ssh.startSession
    try {
      val strippedCmdStr = cmd.replaceAll("\r", "").replaceAll("\n", " ")
      val run = session.exec(strippedCmdStr)

      val errorStream = new StreamLog(run.getErrorStream)
      val infoStream = new StreamLog(run.getInputStream)
      errorStream.start()
      infoStream.start()

      errorStream.join(Long.MaxValue)
      infoStream.join(Long.MaxValue)

    } finally session.close


  }

  def download(from: String,target : String) : Unit = {

    try
      ssh.newSCPFileTransfer.download(from, new FileSystemFile(target))
    catch {
      case e: Exception => e.printStackTrace()
    }

  }

  def upload(src: String, target: String): Unit = {

    import net.schmizz.sshj.xfer.FileSystemFile

    try {
      ssh.newSCPFileTransfer.upload(new FileSystemFile(src), target)
    } catch {
      case e: Exception => e.printStackTrace()
    }

  }

}

class StreamLog(stream : InputStream) extends Thread {

  override def run(): Unit = {
    val scanner = new Scanner(stream, "UTF-8").useDelimiter("\n")
    val streamReader = new InputStreamReader(stream)
    while (scanner.hasNext) {
      println(scanner.next())
    }
    scanner.close()
  }

}