package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.task.SSHConfig
import com.jetprobe.core.annotation.{PipelineMeta, TestSuite}
import com.jetprobe.core.structure.{ExecutablePipeline, PipelineBuilder}

import scala.concurrent.duration._

/**
  * @author Shad.
  */
@PipelineMeta(name = "SSH Testing")
class SSHTestSuite extends TestPipeline {

  val sshConfig = SSHConfig(user = "username", password = "password", hostName = "xxx.xx.xx.xx")

  override def tasks: PipelineBuilder =
    pause("Wait for 1 sec",1.seconds)


}
