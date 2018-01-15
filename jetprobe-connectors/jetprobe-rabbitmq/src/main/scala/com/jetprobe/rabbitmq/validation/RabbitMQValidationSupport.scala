package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model.{ExchangeProps, ExchangeQuery, QueueProps, QueueQuery}
import com.jetprobe.rabbitmq.storage.RabbitMQBroker


/**
  * @author Shad.
  */
trait RabbitMQValidationSupport {


  /*def given(exchange : ExchangeQuery)(fnRule : ExchangeProps => Any) : ValidationRule[RabbitMQBroker] = {
    ExchangeValidationRule(fnRule,Expr(exchange.vHost),Expr(exchange.name))
  }*/

 /* def given(queue : QueueQuery)( fnRule : QueueProps => Any ) : ValidationRule[RabbitMQBroker] = {
    QueueValidationRule(fnRule,vhost = Expr(queue.vHost),queueName = Expr(queue.name))
  }*/




}



