package com.jetprobe.core.structure

import java.util.function.Consumer

import com.jetprobe.core.task.builder.{SSHTaskBuilder, SecuredClient, TaskBuilder}
import com.jetprobe.core.task.SSHConfig

/**
  * @author Shad.
  */
trait SecureShell[B] extends Execs[B] {

  def ssh(description: String, config: SSHConfig)(fn: SecuredClient => Unit): B =
    exec(new SSHTaskBuilder(description, fn, config))

  def jssh(description: String, config: SSHConfig)(fn: Consumer[SecuredClient]) : B = {
    /*val anonFn = new Function1[SecuredClient,Unit] {
      override def apply(v1: SecuredClient): Unit = fn.accept(v1)
    }*/
    ssh(description,config)(fn.accept _)
  }

  implicit def toConsumerUnit[A](function: A => Unit): Consumer[A] = new Consumer[A]() {
    override def accept(arg: A): Unit = function.apply(arg)
  }
  //java dsl


  def sshAll(description: String, configs: SSHConfig*)(fn: SecuredClient => Unit): B = {
    val sshTasks = configs.map(conf => new SSHTaskBuilder(s"${description} on ${conf.hostName}", fn, conf))
    exec(sshTasks)
  }

  def defaultCapture: String => Any = println(_)

}