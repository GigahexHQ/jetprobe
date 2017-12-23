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
class MyTestScenario extends TestScenario with RabbitMQValidationSupport with MongoValidationSupport with HttpValidationSupport with ConsulValidationSupport {

  //Declare the rabbitmq connection
  val rabbit = RabbitMQSink("${rabbit.host}")
  val mongo = MongoSink("mongodb://${mongo.host}/")

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
    .exec(
      mongo.createCollection(db = "sample", collection = "employee")
    )
    .pause(1.seconds)

    /*.validate[HttpRequestBuilder](getPosts) {


    val responseValiation = Seq(
      checkHttpResponse(202, _.status)
    )
    val jsonValidations = given(jsonQuery = "$.userId")(
      checkExtractedValue(true, x => x.startsWith("100"))
    )
    responseValiation ++ jsonValidations
  }

    .validate[MongoSink](mongo) {

    val isDbValid = given(mongo(database = "${mdm.tenantId}"))(
      checkDBStats[Boolean](true, dbStats => dbStats.collections == 10)
    )

    val isCollectionCreated = given(mongo(database = "articleAnalytics", collection = "popularityIndex"))(
      checkCollectionStats[Boolean](true, collectionInfo => collectionInfo.nindexes == 2)
    )

    val isDocsInserted = given(mongo(
      database = "articleAnalytics",
      collection = "popularityIndex",
      query = """{"category" : "functional-programming"}""")
    )(
      checkDocuments[Int](100, docs => docs.count)
    )

    isDbValid ++ isCollectionCreated ++ isDocsInserted
  }*/
    .pause(1.minute)
    //Add some more tests
    .build
}
