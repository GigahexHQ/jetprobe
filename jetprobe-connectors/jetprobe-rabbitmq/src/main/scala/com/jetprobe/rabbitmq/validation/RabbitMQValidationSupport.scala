package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model.{ExchangeProps, ExchangeQuery, QueueProps, QueueQuery}
import com.jetprobe.rabbitmq.sink.RabbitMQSink


/**
  * @author Shad.
  */
trait RabbitMQValidationSupport {


  def rabbitMQ(connectionString: String): RabbitMQSink = RabbitMQSink(connectionString)

  def given(exchange : ExchangeQuery)(fnRule : ExchangeProps => Any) : ValidationRule[RabbitMQSink] = {
    ExchangeValidationRule(fnRule,Expr(exchange.vHost),Expr(exchange.name))
  }

  def given(queue : QueueQuery)( fnRule : QueueProps => Any ) : ValidationRule[RabbitMQSink] = {
    QueueValidationRule(fnRule,vhost = Expr(queue.vHost),queueName = Expr(queue.name))
  }




  implicit object RabbitExecutor extends RabbitMQValidator

}



