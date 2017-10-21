package com.jetprobe.rabbitmq.validation

import com.jetprobe.rabbitmq.model.{ExchangeProps, QueueProps}
import com.jetprobe.rabbitmq.sink.RabbitMQSink

/**
  * @author Shad.
  */
object RabbitMQValidationSupport {


  implicit def rabbitToValidation(sink : RabbitMQSink) : RabbitMQValidationRuleBuilder = new RabbitMQValidationRuleBuilder(sink)

  def checkExchange[U](expected : U, actual : ExchangeProps => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  ExchangeValidationRule[U] = ExchangeValidationRule(expected, actual,fullName = fullName,line = line)

  def checkQueue[U](expected : U, actual : QueueProps => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  QueueValidationRule[U] = QueueValidationRule(expected, actual,fullName = fullName,line = line)

  def rabbitMQ(connectionString : String) : RabbitMQSink = RabbitMQSink(connectionString)

  implicit object RabbitExecutor extends RabbitMQValidator

}


