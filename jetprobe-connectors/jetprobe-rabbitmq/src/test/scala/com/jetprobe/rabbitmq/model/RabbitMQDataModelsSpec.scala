package com.jetprobe.rabbitmq.model

import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
  * @author Shad.
  */
class RabbitMQDataModelsSpec extends FlatSpec with BeforeAndAfter{

  behavior of "ExchangeProps case class"

  it should "right props 1" in {
    val exchangeProps = ExchangeProps("amqp.topic","topic",true,true)
    assert(exchangeProps.durable)
    assert(!exchangeProps.isAutoDelete)
  }

  it should "fetch props 2" in {
    val exchangeProps = ExchangeProps("amqp.topic","topic",true,true)
    assert(exchangeProps.durable)
    assert(!exchangeProps.isAutoDelete)
  }

  behavior of "QueueProps case class"

  it should "fetch right props" in {
    val qprops = QueueProps("q1",false,true)
    assert(!qprops.durable)
    assert(qprops.name.startsWith("q1"))
  }

  behavior of "Queue binding case class"

  it should "fetch right props" in {
    val binding = QueueBinding("q1","event.create",Map.empty)
    assert(binding.to.equalsIgnoreCase("q1"))
    assert(binding.routingKey.contains("create"))
  }

}
