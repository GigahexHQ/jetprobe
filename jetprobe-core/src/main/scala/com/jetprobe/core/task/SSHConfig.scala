package com.jetprobe.core.task

import akka.actor.Props
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import java.util.concurrent.TimeUnit

import com.jetprobe.core.task.builder.SSHMessage
import com.jetprobe.core.session.Session
import net.schmizz.sshj.connection.channel.direct
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

/**
  * @author Shad.
  */


case class SSHConfig(hostName: String,
                     user: String,
                     password: String)
