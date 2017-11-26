package com.jetprobe.sample

import com.jetprobe.consul.validation.ConsulValidationSupport
import com.jetprobe.core.Predef.fromFile
import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.extractor.JsonPathExtractor.jsonPath
import com.jetprobe.core.http.validation.HttpValidationSupport
import com.jetprobe.core.http.{Http, HttpRequestBuilder}
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.mongo.sink.MongoSink
import com.jetprobe.mongo.validation.MongoValidationSupport
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport

import scala.concurrent.duration._

/**
  * @author Shad.
  */
//@TestSuite
class MyTestScenario extends TestScenario with RabbitMQValidationSupport with MongoValidationSupport with HttpValidationSupport with ConsulValidationSupport{

  //Declare the rabbitmq connection
  val rabbit = RabbitMQSink("${rabbit.host}")
  val mongo = MongoSink("mongodb://${mongo.host}/hHTk8DHiIKhlTXuU4ewN8V/")

  val getPosts = Http("getPosts")
    .get("https://${server.hostname}/posts/1")
    .extract(
      jsonPath("$.userId", saveAs = "userId"),
      jsonPath("$.title", saveAs = "title")
    )

  val insertPost = Http("insertionPost")
    .post("https://reqres.in/api/users")
    .body(
      fromFile("C:\\Users\\samez\\Documents\\match-service\\post_req.json")
    )
    .header("Content-type", "application/json")
    .extract(
      jsonPath("$.name", saveAs = "username"),
      jsonPath("$.id", saveAs = "id")
    )

  override def buildScenario: ExecutableScenario = scenario("PauseScn")
    .http(insertPost)
    .pause(3.seconds)
    .http(getPosts)
    .pause(1.seconds)

    .validate[HttpRequestBuilder](getPosts) {


   val responseValiation = Seq(
     checkHttpResponse(202, _.status)
   )
    val jsonValidations = given(jsonQuery = "$.userId")(
      checkExtractedValue(true, x => x.startsWith("100"))
    )
      responseValiation ++ jsonValidations
  }

    .validate[MongoSink](mongo) {

    val serverValidations = Seq(
      checkStats[String]("3.4.0", _.version),
      checkStats[Boolean](true, _.version.startsWith("3.4")),
      checkStats[Boolean](true, _.connections.current < 50),
      checkStats[Long](80L, _.opcounters.insert)
    )

    val databaseStats = given(mongo("${mdm.tenantId}"))(
      checkDBStats[Boolean](true, _.collections == 10)
    )

    serverValidations ++ databaseStats


  }

    .pause(3.seconds)
    //Add some more tests
    .build
}
