package com.jetprobe.core.action

import akka.actor.Props
import com.jetprobe.core.action.SSHAction.SSHMessage
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import java.util.concurrent.TimeUnit

import net.schmizz.sshj.transport.verification.PromiscuousVerifier

/**
  * @author Shad.
  */
class SSHActor(actionDef: SSHActionDef) extends BaseActor {

  override def receive: Receive = {
    case SSHMessage(config, session, next) =>


      val sshClient = new SSHClient()
      try{
        sshClient.addHostKeyVerifier(new PromiscuousVerifier)
        sshClient.connect(config.hostName)
        sshClient.authPassword(config.user, config.password)
        run(actionDef, sshClient)
        next ! session
      }catch {
        case ex : Exception =>
          logger.error(ex.getMessage)
          next ! session
      }


  }

  def run(actionDef: SSHActionDef, ssh: SSHClient): Unit = {

    actionDef match {
      case ExecuteCmd(cmdStr) =>
        try {
          val session = ssh.startSession
          try {
            val cmd = session.exec(cmdStr)
            logger.info("\n" +IOUtils.readFully(cmd.getInputStream).toString)
            logger.info("\n** exit status: " + cmd.getExitStatus)
          } finally session.close
        } finally ssh.disconnect


    }


  }
}

object SSHActor {

  def props(actionDef: SSHActionDef): Props = Props(new SSHActor(actionDef))

}