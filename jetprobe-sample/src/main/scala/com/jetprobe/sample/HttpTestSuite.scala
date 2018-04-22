package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.{PipelineMeta}
import com.jetprobe.core.http.Http
import com.jetprobe.core.structure.PipelineBuilder

/**
  * @author Shad.
  */
@PipelineMeta(name = "Http Testing")
class HttpTestSuite extends TestPipeline{

  val getPosts = Http("Sample request")
    .get("http://example.com/api/v1/posts/1")

  override def tasks: PipelineBuilder = {

      http(getPosts)


  }

}
