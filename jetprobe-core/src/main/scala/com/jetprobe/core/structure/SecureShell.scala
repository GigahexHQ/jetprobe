package com.jetprobe.core.structure

import com.jetprobe.core.task.builder.{TaskBuilder, SSHTaskBuilder, SecuredClient}
import com.jetprobe.core.task.SSHConfig

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B] {

  def ssh(description: String, config: SSHConfig)(fn: SecuredClient => Unit): B =
    exec(new SSHTaskBuilder(description, fn, config))

  def sshAll(description: String, configs: SSHConfig*)(fn: SecuredClient => Unit): B = {
    val sshTasks = configs.map(conf => new SSHTaskBuilder(s"${description} on ${conf.hostName}", fn, conf))
    exec(sshTasks)
  }

  def defaultCapture: String => Any = println(_)

}