package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model.{ExchangeProps, QueueProps}
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import sourcecode.{FullName, Line}

/**
  * @author Shad.
  */
case class ExchangeValidationRule[U <: Any](expected: U,
                                     actual: (ExchangeProps) => U,
                                     vHost: String = "/",
                                     exchangeName: String = "fanout.ex",
                                     line: Line,
                                     fullName: FullName) extends ValidationRule[RabbitMQSink] {
  def name = s"Validation for exchange : $exchangeName"

}

case class QueueValidationRule[U <: Any](expected: U,
                                  actual: (QueueProps) => U,
                                  vhost: String = "",
                                  queueName: String = "",
                                  line: Line,
                                  fullName: FullName) extends ValidationRule[RabbitMQSink] {

  def name = s"Validation for Queue : $queueName in virtual host : $vhost"
}

//case class MessageValidationRule
