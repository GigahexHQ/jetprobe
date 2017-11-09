package com.jetprobe.sample

import com.jetprobe.core.Predef.fromFile
import com.jetprobe.core.TestScenario
import com.jetprobe.core.extractor.JsonPathExtractor.jsonPath
import com.jetprobe.core.http.{Http, HttpRequestBuilder}
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.mongo.sink.MongoSink
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport._
import com.jetprobe.mongo.validation.MongoValidationSupport._
import com.jetprobe.core.http.validation.HttpValidationSupport._

import scala.concurrent.duration._

/**
  * @author Shad.
  */

class MyTestScenario extends TestScenario {

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

  override def buildScenario: ExecutableScenario = {

    scenario("PauseScn")
      .http(insertPost)
      .pause(3.seconds)
      .http(getPosts)
      .pause(1.seconds)
      .validate[HttpRequestBuilder](getPosts) { httpPost =>

      httpPost.forHttpRequest(
        checkHttpResponse(202, _.status)
      ) ++
        httpPost.forJsonQuery("$.userId")(
          checkExtractedValue("1", x => x)
        )

    }

      .validate[RabbitMQSink](rabbit) { rbt =>
      rbt.forExchange(exchange = "amq.direct", vHost = "/")(
        checkExchange[String]("direct", exchangProps => exchangProps.exchangeType),
        checkExchange[Int](1, _.bindings.size),
        checkExchange[Int](1, _.bindings.size),
        checkExchange[Boolean](false, _.bindings.exists(_.to.startsWith("mdm.match-api")))
      ) ++
        rbt.forQueue(queue = "mdm.business-entity.dispatch.api", vHost = "message")(
          checkQueue[Boolean](true, _.autoDelete),
          checkQueue[Boolean](true, _.durable)

        )
    }

      .validate[MongoSink](mongo) { mng =>
      mng.forServer(
        checkStats[String]("3.4.0", _.version),
        checkStats[Boolean](true, _.version.startsWith("3.4")),
        checkStats[Boolean](true, _.connections.current < 50),
        checkStats[Long](80L, _.opcounters.insert)
      )
    }

      .pause(3.seconds)
      //Add some more tests
      .build
  }
}
