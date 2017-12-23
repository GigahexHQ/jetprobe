package com.jetprobe.sample

import com.jetprobe.core.{JTestScenario, TestScenario}
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.extractor.JsonPathExtractor.jsonPath
import com.jetprobe.core.http.validation.HttpValidationSupport
import com.jetprobe.core.http.{Http, HttpRequestBuilder}
import com.jetprobe.core.structure.ExecutableScenario
import com.typesafe.scalalogging.LazyLogging
import org.junit.Assert._
import scala.concurrent.duration._

/**
  * @author Shad.
  */


@TestSuite
class HttpTestSuite extends TestScenario with HttpValidationSupport with LazyLogging{

  val getPosts = Http("getPosts").get("https://reqres.in/api/users/4")


  override def buildScenario: ExecutableScenario = {

    scenario("Http request tests")
      .pause(2.seconds)
      .http(getPosts)
      .validate[HttpRequestBuilder](getPosts)(

        given(getPosts.havingJsonQuery("$.data.first_name")) { value =>

          assertEquals("Eve", value)
          assertEquals(9/3, value.length)

        },
      given(getPosts.havingJsonQuery("$.data.id")) { value =>

        logger.info(value)
        assertEquals("4", value)
        assertEquals(13, value.length)

      }

      )
      .pause(1.second)
      .build()

  }
}

class PauseSuite extends TestScenario {

  override def buildScenario: ExecutableScenario =

    scenario("Pause test suite")
      .pause(2.seconds)
      .pause(2.seconds)
      .build()

}
