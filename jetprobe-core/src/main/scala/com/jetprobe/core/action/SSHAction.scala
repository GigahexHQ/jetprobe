package com.jetprobe.core.action

import akka.actor.ActorRef
import com.jetprobe.core.action.SSHAction.SSHMessage
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
class SSHAction(config: SSHConfig, actor : ActorRef,next : Action) extends Action{

  override def name: String = s"ssh-actor@${config.hostName}"

  override def execute(session: Session): Unit = actor ! SSHMessage(config,session,next)
}

object SSHAction {

  case class SSHMessage(sshConfig : SSHConfig,session: Session,next : Action)

}

trait SSHActionDef

case class SshCopy(from: String,to : String) extends SSHActionDef
case class ExecuteCmd(cmd : String) extends SSHActionDef

case class SSHConfig(hostName : String,
                     user : String,
                     password : String)