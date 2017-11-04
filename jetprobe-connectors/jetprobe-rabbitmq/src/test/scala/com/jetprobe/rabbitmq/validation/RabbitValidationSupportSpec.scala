package com.jetprobe.rabbitmq.validation

import com.jetprobe.rabbitmq.sink.RabbitMQSink
import org.scalatest.FlatSpec

/**
  * @author Shad.
  */
class RabbitValidationSupportSpec extends FlatSpec{

  behavior of "RabbitValidationSpec"

  it should "create rabbit validationRules" in {
    import RabbitMQValidationSupport._
    val sink = RabbitMQSink("10.255.255.255")

    val queueRules = sink.forQueue("q1","hostName")(
      checkQueue[Boolean](true,_.autoDelete)
    )

    val exchangeRules = sink.forExchange("amq.topic")(
      checkExchange[String]("amqp.topic",_.name),
      checkExchange[Boolean](true,_.isAutoDelete)
    )

    assert(queueRules.size === 3)
    assert(exchangeRules === queueRules)
  }

}
