package com.jetprobe.core.action.builder

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.Scanner

import akka.actor.ActorSystem
import com.jetprobe.core.action._
import com.jetprobe.core.structure.ScenarioContext
import com.typesafe.scalalogging.LazyLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile


/**
  * @author Shad.
  */
class SSHActionBuilder(fnShellAction: SecuredClient => Unit, sSHConfig: SSHConfig) extends ActionBuilder with LazyLogging {

  private val name = "Secured-Shell-Action"

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val sshMessage = SSHMessage(fnShellAction, sSHConfig, next)


    new SelfExecutableAction(name, sshMessage, next, ctx.system, ctx.controller)({
      case (message, sess) => message match {
        case SSHMessage(fnAction, config, next) =>

          val sshClient = new SSHClient()
          sshClient.addHostKeyVerifier(new PromiscuousVerifier)
          sshClient.connect(config.hostName)
          sshClient.setConnectTimeout(5)
          sshClient.authPassword(config.user, config.password)
          try {
            fnAction(new SecuredClient(sshClient,ctx.system))
          } finally sshClient.disconnect

          //return the same session
          sess

      }
    }
    )
  }


}

case class SSHMessage(fnShellAction: SecuredClient => Unit, sshConfig: SSHConfig, next: Action) extends ActionMessage {

  override def name: String = s"SSH Actions at host : ${sshConfig.hostName}"
}

class SecuredClient(ssh: SSHClient,actorSystem: ActorSystem) extends LazyLogging {


  def run(cmd: String): Unit = {

    val session = ssh.startSession
    try {
      val run = session.exec(cmd.replaceAll("\r", "").replaceAll("\n", " "))

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