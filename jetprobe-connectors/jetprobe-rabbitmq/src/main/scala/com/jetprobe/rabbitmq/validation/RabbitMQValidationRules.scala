package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model.{ExchangeProps, QueueProps}
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import sourcecode.{FullName, Line}

/**
  * @author Shad.
  */
case class ExchangeValidationRule[U <: Any](expected: U,
                                     actual: (ExchangeProps) => U,
                                     vHost: Expr = Expr(),
                                     exchangeName: Expr = Expr(),
                                     line: Line,
                                     fullName: FullName) extends ValidationRule[RabbitMQSink] {
  def name = s"Validation for exchange : ${exchangeName.value}"


}

case class QueueValidationRule[U <: Any](expected: U,
                                  actual: (QueueProps) => U,
                                  vhost: Expr = Expr(),
                                  queueName: Expr = Expr(),
                                  line: Line,
                                  fullName: FullName) extends ValidationRule[RabbitMQSink] {

  def name = s"Validation for Queue : ${queueName.value} in virtual host : ${vhost.value}"
}

//case class MessageValidationRule
