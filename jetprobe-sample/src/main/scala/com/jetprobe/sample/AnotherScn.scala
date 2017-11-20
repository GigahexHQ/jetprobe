package com.jetprobe.sample

import com.jetprobe.core.TestScenario
import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationSupport

import scala.concurrent.duration._

/**
  * @author Shad.
  */
class AnotherScn extends TestScenario with RabbitMQValidationSupport{

  val rabbit = RabbitMQSink("${rabbit.host}")

  override def buildScenario: ExecutableScenario = scenario("Second").
    pause(4.seconds)
    /*.validate[RabbitMQSink](rabbit) { rbt =>

    val exchangeTests = rbt.forExchange(exchange = "amq.direct", vHost = "/")(
      checkExchange[String]("direct", exchangProps => exchangProps.exchangeType),
      checkExchange[Int](1, _.bindings.size),
      checkExchange[Int](1, _.bindings.size),
      checkExchange[Boolean](false, _.bindings.exists(_.to.startsWith("mdm.match-api")))
    )

   val queueTests =  rbt.forQueue(queue = "mdm.business-entity.dispatch.api", vHost = "message")(
      checkQueue[Boolean](true, _.autoDelete),
      checkQueue[Boolean](true, _.durable)
    )

    exchangeTests ++ queueTests
  }*/
    .build

}
