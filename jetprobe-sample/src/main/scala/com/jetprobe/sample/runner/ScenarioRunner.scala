package com.jetprobe.sample.runner

import akka.actor.ActorSystem
import com.jetprobe.core.runner.Runner
import com.jetprobe.sample._

/**
  * @author Shad.
  */
object ScenarioRunner extends App{

  implicit val actorSystem = ActorSystem("run-test-suites")

  val mongoSuite = new MongoSuite

  val httpSuite = new HttpTestSuite

  val sshSuite = new SSHTestSuite

  val pauseSuite = new PauseSuite

  val hdfsSuite = new HDFSSuite

  val hBaseSuite = new HBaseSuite

  val loadScn = Seq.fill(1)(hBaseSuite.buildScenario)

  Runner().run(loadScn)



}
