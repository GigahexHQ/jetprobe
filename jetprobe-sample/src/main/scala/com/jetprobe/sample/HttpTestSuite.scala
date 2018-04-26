package com.jetprobe.sample

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.PipelineMeta
import com.jetprobe.core.http.Http
import com.jetprobe.core.http.validation.HttpValidationSupport
import com.jetprobe.core.structure.PipelineBuilder

/**
  * @author Shad.
  */
@PipelineMeta(name = "Fast Http Testing")
class HttpTestSuite extends TestPipeline with HttpValidationSupport{

  val getPosts = Http("Sample request")
    .get("https://reqres.in/api/users/2")

  override def tasks: PipelineBuilder = {

      http(getPosts)


      validateGiven("Testing Janet user",getPosts) { res =>

        given[String]("$.data.first_name") { resp =>
          assertEquals("Janet",resp)

        }
      }

    validateGiven("Testing a failed test",getPosts) { res =>

      given[String]("$.data.first_name") { resp =>
        assertEquals("Janet2",resp)

      }
    }


  }

}
