package com.jetprobe.core.structure

import com.jetprobe.core.task.builder.{TaskBuilder, SSHTaskBuilder, SecuredClient}
import com.jetprobe.core.task.SSHConfig

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B]{

  def ssh(description : String,config : SSHConfig)(fn : SecuredClient => Unit) : B =
    exec(new SSHTaskBuilder(description,fn,config))

  def defaultCapture : String => Any = println(_)

}