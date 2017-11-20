package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model.{ExchangeProps, ExchangeQuery, QueueProps, QueueQuery}
import com.jetprobe.rabbitmq.sink.RabbitMQSink


/**
  * @author Shad.
  */
trait RabbitMQValidationSupport {


  def checkExchange[U](expected: U, actual: ExchangeProps => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  ExchangeValidationRule[U] = ExchangeValidationRule(expected, actual, fullName = fullName, line = line)


  def checkQueue[U](expected: U, actual: QueueProps => U)(implicit line: sourcecode.Line, fullName: sourcecode.FullName):
  QueueValidationRule[U] = QueueValidationRule(expected, actual, fullName = fullName, line = line)

  def rabbitMQ(connectionString: String): RabbitMQSink = RabbitMQSink(connectionString)

  def given(exchange : ExchangeQuery)(rules : ExchangeValidationRule[_]*) : Seq[ValidationRule[RabbitMQSink]] = {
    rules.map( ex => ex.copy(exchangeName = Expr(exchange.name), vHost = Expr(exchange.vHost)))
  }

  def given(queue : QueueQuery)( rules : QueueValidationRule[_]* ) : Seq[ValidationRule[RabbitMQSink]] = {
    rules.map( ex => ex.copy(queueName = Expr(queue.name), vhost = Expr(queue.vHost)))
  }


  def exchange(name : String,vHost : String) : ExchangeQuery = new ExchangeQuery(name,vHost)
  def queue(name : String, vHost : String) : QueueQuery = new QueueQuery(name,vHost)

  implicit object RabbitExecutor extends RabbitMQValidator

}



