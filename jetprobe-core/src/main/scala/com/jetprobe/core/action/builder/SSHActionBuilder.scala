package com.jetprobe.core.action.builder

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.jetprobe.core.action.SSHActor.SSHMessage
import com.jetprobe.core.action._
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.ScenarioContext
import com.typesafe.scalalogging.LazyLogging
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

/**
  * @author Shad.
  */
class SSHActionBuilder(actionDef: SSHActionDef, sSHConfig: SSHConfig) extends ActionBuilder with LazyLogging {

  private val name = "Secured-Shell-Action"

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  /*override def build(ctx: ScenarioContext, next: Action): Action = {
    val sshActor = ctx.system.actorOf(SSHActor.props(next),"SSHActor-" + new UUIDGenerator().generateId(this).toString)
    val sshMessage = SSHMessage(actionDef,sSHConfig,next)
    new ExecutableAction(sshMessage,sshActor)
  }*/

  override def build(ctx: ScenarioContext, next: Action): Action = {
    val sshMessage = SSHMessage(actionDef, sSHConfig, next)

    new SelfExecutableAction(name, sshMessage, next, ctx.system,ctx.controller)({
        case (message, sess) => message match {
          case SSHMessage(adef, config, next) =>

            val sshClient = new SSHClient()
            sshClient.addHostKeyVerifier(new PromiscuousVerifier)
            sshClient.connect(config.hostName)
            sshClient.setConnectTimeout(5)
            sshClient.authPassword(config.user, config.password)
            run(adef, sshClient)
            sess

        }
      }
    )
  }



  def run(actionDef: SSHActionDef, ssh: SSHClient): Unit = {

    actionDef match {
      case ExecuteCmd(cmdStr) =>
        try {
          val session = ssh.startSession
          try {
            val cmd = session.exec(cmdStr)
            logger.info("\n" + IOUtils.readFully(cmd.getInputStream).toString)
            logger.info("\n** exit status: " + cmd.getExitStatus)
          } finally session.close
        } finally ssh.disconnect

    }
  }

}
