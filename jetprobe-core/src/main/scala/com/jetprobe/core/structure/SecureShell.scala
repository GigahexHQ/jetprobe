package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.{ActionBuilder, SSHActionBuilder, SecuredClient}
import com.jetprobe.core.action.{ExecuteCmd, ExecuteCommand, SSHConfig}

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B]{

  def ssh(config : SSHConfig)(fn : SecuredClient => Unit) : B = exec(new SSHActionBuilder(fn,config))


}