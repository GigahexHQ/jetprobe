package com.jetprobe.core.structure

import com.jetprobe.core.task.builder.{TaskBuilder, SSHTaskBuilder, SecuredClient}
import com.jetprobe.core.task.SSHConfig

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B]{

  def ssh(config : SSHConfig)(fn : SecuredClient => Unit) : B = exec(new SSHTaskBuilder(fn,config))


}