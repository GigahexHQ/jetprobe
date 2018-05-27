package com.jetprobe.core.flow

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.jetprobe.core.flow.JobController.StartJobExecution
import com.jetprobe.core.task.Failed
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * @author Shad.
  */
class JobControllerActorSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }


  "JobController " must {
    "start Job execution" in {

      val jobMeta = JobController.buildFromConfig("testScn.yml",None)
      val jc = system.actorOf(JobController.props(jobMeta.right.get._1, jobMeta.right.get._2,jobMeta.right.get._3))

      jc ! StartJobExecution
      expectMsg(Failed)

    }
  }
}
