package com.jetprobe.core.action

import akka.actor.Props
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import java.util.concurrent.TimeUnit

import com.jetprobe.core.action.SSHActor.SSHMessage
import com.jetprobe.core.session.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

/**
  * @author Shad.
  */
class SSHActor(val next : Action) extends ActionActor {

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

  override def execute(actionMessage: ActionMessage, session: Session): Unit = actionMessage match {
    case SSHMessage(actionDef,config, next) =>

      val sshClient = new SSHClient()
        sshClient.addHostKeyVerifier(new PromiscuousVerifier)
        sshClient.connect(config.hostName)
        sshClient.setConnectTimeout(5)
        sshClient.authPassword(config.user, config.password)
        run(actionDef, sshClient)
        next ! session

  }
}

trait SSHActionDef

case class SshCopy(from: String, to: String) extends SSHActionDef

case class ExecuteCmd(cmd: String) extends SSHActionDef

case class SSHConfig(hostName: String,
                     user: String,
                     password: String)

object SSHActor {

  case class SSHMessage(actionDef: SSHActionDef,sshConfig: SSHConfig, next: Action) extends ActionMessage {

    override def name: String = s"SSH Action at host : ${sshConfig.hostName}"
  }


  def props(next : Action): Props = Props(new SSHActor(next))

}