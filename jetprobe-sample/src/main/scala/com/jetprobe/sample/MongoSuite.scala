package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.extractor.JsonPathExtractor.jsonPath
import com.jetprobe.core.http.Http
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.mongo.sink.MongoSink

import scala.concurrent.duration._

/**
  * @author Shad.
  */
@TestSuite
class MongoSuite extends TestScenario {

  val mongoDB = MongoSink("mongodb://${mongo.host}/")

  val getPosts = Http("getPosts")
    .get("https://${server.hostname}/posts/1")
    .extract(
      jsonPath("$.userId", saveAs = "userId"),
      jsonPath("$.title", saveAs = "title")
    )

  override def buildScenario: ExecutableScenario = {
    scenario("Mongo Test suite")
      .http(getPosts)
      .exec(
        mongoDB.createCollection("some", "employee2")
      )
      .exec(
        mongoDB.createDatabase("interesting-database")
      )
      .exec(
        mongoDB.dropCollection("some", "employee")
      )
      .exec(
        mongoDB.insertDocuments(db = "some", collection = "employee32", rows = Seq("""{_id : 1}""","""{_id : 2}"""))
      )
      .exec(
        mongoDB.insertDocuments(db = "some", collection = "employee3", rows = Seq("""{_id : 1}""","""{_id : 2}""","""{_id : 3}"""))
      )
      .exec(
        mongoDB.createCollection("anotherdb", "colll", Seq("emp.name"))
      )
      .exec(
        mongoDB.removeAllDocuments("some", "employee32")
      )
      .pause(2.seconds)
      .build
  }
}
