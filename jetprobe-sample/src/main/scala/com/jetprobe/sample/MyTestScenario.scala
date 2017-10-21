package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.mongo.sink.MongoSink
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport._
import com.jetprobe.mongo.validation.MongoValidationSupport._

import scala.concurrent.duration._

/**
  * @author Shad.
  */

class MyTestScenario extends TestScenario {

  //Declare the rabbitmq connection
  val rabbit = RabbitMQSink("192.168.1.6")
  val mongo = MongoSink("mongodb://192.168.1.6/hHTk8DHiIKhlTXuU4ewN8V/xref.customer")


  override def buildScenario: ExecutableScenario = {

    scenario("PauseScn")
      .pause(1.seconds)

      .validate[RabbitMQSink](rabbit) { rbt =>
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
