package com.jetprobe.core.flow

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
  * @author Shad.
  */
class JobControllerParserSpec extends FlatSpec with Matchers{


  "Config from external File" should "parse and fetch the config" in {
    val f = getClass.getResource("/testScn.yml").getPath
    val jobMeta = JobController.buildFromConfig(f,None)

    assert(jobMeta.isRight)
    assert(jobMeta.right.get._1.size == 1)

    val scn = jobMeta.right.get._1.dequeue()
    assert(scn.pipelines.size == 1)
    assert(scn.name.equals("TestScenario"))
    assert(scn.project.endsWith("ness"))
    assert(scn.tags.head.equals("fast"))

  }

  "Config from external File" should "fail while parsing invalid file" in {

    val jobMeta = JobController.buildFromConfig("/path/to/invalid/file.yml",None)
    assert(jobMeta.isLeft)
    assert(jobMeta.left.get.getMessage.contains("Unable to parse"))

  }

  "Config from internal File" should "parse successfully" in {

    val jobMeta = JobController.buildFromConfig("testScn.yml",None)

    assert(jobMeta.isRight)
    assert(jobMeta.right.get._1.size == 1)

    val scn = jobMeta.right.get._1.dequeue()
    assert(scn.pipelines.size == 1)
    assert(scn.name.equals("TestScenario"))
    assert(scn.project.endsWith("ness"))
    assert(scn.tags.head.equals("fast"))

  }



}
