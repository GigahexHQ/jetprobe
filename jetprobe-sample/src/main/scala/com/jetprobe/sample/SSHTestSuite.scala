package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.action.SSHConfig
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.structure.ExecutableScenario
import scala.concurrent.duration._

/**
  * @author Shad.
  */
@TestSuite
class SSHTestSuite extends TestScenario {

  val sshConfig = SSHConfig(user = "shad", password = "admin", hostName = "192.168.37.128")

  override def buildScenario: ExecutableScenario =
    scenario("shell testing")
      .ssh(sshConfig, cmd = "cd /home/shad/apps && ls -lrt")
      .build

}
