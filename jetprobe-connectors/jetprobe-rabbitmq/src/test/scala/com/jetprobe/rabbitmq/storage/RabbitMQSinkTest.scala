package com.jetprobe.rabbitmq.storage

import org.scalatest.FlatSpec
import org.scalatest._
/**
  * @author Shad.
  */
class RabbitMQSinkTest extends FlatSpec {

  behavior of "RabbitMQSinkTest"

  it should "have defaultProtocol" in {
    assert(RabbitMQBroker.defaultProtocol == "http")
  }

  it should "defaultPassword" in {
      assert(RabbitMQBroker.defaultPassword == "guest")
  }

  it should "defaultUsername" in {
    assert(RabbitMQBroker.defaultUserName == "guest")
  }




}
