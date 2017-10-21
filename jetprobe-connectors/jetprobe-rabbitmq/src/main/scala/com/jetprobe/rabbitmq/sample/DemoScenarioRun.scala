package com.jetprobe.rabbitmq.sample

import akka.actor.ActorSystem
import com.jetprobe.core.extractor.JsonPathExtractor._
import com.jetprobe.core.http.Http
import com.jetprobe.core.runner.Runner
import com.jetprobe.core.Predef._
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Shad.
  */
object DemoScenarioRun extends App {


  val dataset = fromTemplate(
    "C:\\Users\\samez\\Documents\\match-service\\sample.txt",
    "C:\\Users\\samez\\Documents\\match-service\\data.csv",
    100)

  val defaultDB = "52ZSTB0IDK6dXxaEQLUaQu"
  val coll = "PfObjects"

  val rabbit = RabbitMQSink("192.168.37.128")


  val pipe = scenario("First Test Scenario")
    //.http(insertPost)
    //.pause(3.seconds)
    // .http(getPosts)
    .validate(rabbit) { rbt =>
    rbt.forExchange(exchange = "history_event_data", vHost = "iORXPuAssgckXVq8B4xcwg")(
      checkExchange[String]("direct", ex => ex.exchangeType),
      checkExchange[Int](1, _.bindings.size),
      checkExchange[Boolean](true, _.bindings.exists(_.to.startsWith("mdm.match-api")))

    )

    rbt.forQueue(queue = "mdm.business-entity.dispatch.api", vHost = "iORXPuAssgckXVq8B4xcwg")(
      checkQueue[Boolean](true, _.autoDelete),
      checkQueue[Boolean](true, _.durable)

    )
  }
    //.pause(3.seconds)

    //Add some more tests
    .build

  implicit val actorSystem = ActorSystem()

 // Runner().run(pipe)
  Thread.sleep(3000)
  Await.result(actorSystem.terminate(), 10.seconds)
  //System.exit(0)

}
