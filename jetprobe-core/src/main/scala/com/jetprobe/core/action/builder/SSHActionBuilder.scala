package com.jetprobe.core.action.builder

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.jetprobe.core.action.SSHActor.SSHMessage
import com.jetprobe.core.action._
import com.jetprobe.core.structure.ScenarioContext

/**
  * @author Shad.
  */
class SSHActionBuilder(actionDef : SSHActionDef,sSHConfig: SSHConfig) extends ActionBuilder{
  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val sshActor = ctx.system.actorOf(SSHActor.props(next),"SSHActor-" + new UUIDGenerator().generateId(this).toString)
    val sshMessage = SSHMessage(actionDef,sSHConfig,next)
    new ExecutableAction(sshMessage,sshActor)
  }
}
