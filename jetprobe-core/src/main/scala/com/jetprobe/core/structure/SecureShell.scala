package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.SSHActionBuilder
import com.jetprobe.core.action.{ExecuteCmd, ExecuteCommand, SSHConfig, SshCopy}

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B]{

  def ssh(config : SSHConfig, copyFrom : String, copyTo : String) : B = {
    val copy = SshCopy(copyFrom,copyTo)
    exec(new SSHActionBuilder(copy,config))
  }

  def ssh(config : SSHConfig, cmd : String) : B = {
    val actionBuilder = new SSHActionBuilder(ExecuteCmd(cmd),config)
    exec(actionBuilder)
  }

}