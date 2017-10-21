package com.jetprobe.rabbitmq.sink

import org.scalatest.FlatSpec
import org.scalatest._
/**
  * @author Shad.
  */
class RabbitMQSinkTest extends FlatSpec {

  behavior of "RabbitMQSinkTest"

  it should "have defaultProtocol" in {
    assert(RabbitMQSink.defaultProtocol == "http")
  }

  it should "defaultPassword" in {
      assert(RabbitMQSink.defaultPassword == "guest")
  }

  it should "defaultUsername" in {
    assert(RabbitMQSink.defaultUserName == "guest")
  }




}
