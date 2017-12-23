package com.jetprobe.sample

import java.util

import com.jetprobe.core.TestScenario
import com.jetprobe.core.annotation.TestSuite
import com.jetprobe.core.extractor.JsonPathExtractor.jsonPath
import com.jetprobe.core.http.Http
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.mongo.sink.MongoSink
import com.jetprobe.mongo.validation.MongoValidationSupport
import com.jetprobe.core.extractor.JsonPathExtractor.extractJsonVal
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * @author Shad.
  */
@TestSuite
class MongoSuite extends TestScenario with MongoValidationSupport with RabbitMQValidationSupport with LazyLogging {


  val mongoDB = MongoSink("mongodb://192.168.37.128/")

  val rabbitServer = RabbitMQSink("192.168.37.128")

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


  val getPosts = Http("getPosts").get("https://reqres.in/api/users/2")

  case class Person(_id: Long, name: String, age: Int)


  override def buildScenario: ExecutableScenario = {

    implicit val converter = (x: String) => parse(x).extract[Person]

    scenario("Mongo Test suite")
      .http(insertPost)
      .http(
        new Http("Create Org Request").get("https://${ics.host}:${ics.port}/api/v1")
      )
      //.pause(3.seconds)
      .exec(
      mongoDB.createCollection("some", "employee2")
    )
      .exec(
        mongoDB.createDatabase("interesting-database"),
        mongoDB.dropCollection("some", "employee"),
        mongoDB.insertDocuments(db = "some", collection = "employee32", rows = Seq("""{_id : 1}""","""{_id : 2}"""))
      )
      .exec(
        mongoDB.insertDocuments(db = "employee", collection = "info", rows = Seq(
          """{_id : 1, name : "Shad Amez", age : 35}""",
          """{_id : 2, name : "Shena Mustaq", age : 30}""",
          """{_id : 3, name : "Shohaib Alam", age : 35}"""))
      )
      .exec(
        mongoDB.createCollection("anotherdb", "colll", Seq("emp.name"))
      )

      .validate[MongoSink](mongoDB)(

          given(mongoDB.having(database = "some")) { databaseInfo =>

            assertEquals(3, databaseInfo.indexes)
            assertEquals(2, databaseInfo.collections)

          },

      given(mongoDB.select[Person]("select _id,name,age from employee.info")(parse(_).extract[Person])) { result =>


        assertEquals(3, result.size)

        assertEquals(true, result.forall(_.age > 20))


      }

    )
      .validate[RabbitMQSink](rabbitServer)(

      given(rabbitServer.havingExchange(exchange = "amq.topic", vHost = "/")) { exchangeInfo =>

        assertEquals(true, exchangeInfo.durable)
        assertEquals(0, exchangeInfo.bindings.size)

      }
    )
      .exec(
        mongoDB.removeAllDocuments("some", "employee32")
      )

      .build
  }
}

