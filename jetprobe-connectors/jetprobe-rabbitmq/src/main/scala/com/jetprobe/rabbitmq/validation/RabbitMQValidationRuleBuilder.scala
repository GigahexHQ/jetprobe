package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.{ValidationRule, ValidationRulesBuilder}
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidationRuleBuilder.rules

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class RabbitMQValidationRuleBuilder(sink : RabbitMQSink) extends ValidationRulesBuilder[RabbitMQSink]{

  import RabbitMQValidationRuleBuilder._

  val rules: ArrayBuffer[ValidationRule[RabbitMQSink]] = ArrayBuffer.empty

  def forExchange(exchange : String, vHost : String = "/")( ruleBuilders : ExchangeValidationRule[_]* ) : Seq[ValidationRule[RabbitMQSink]] = {
     ruleBuilders.map( ex => ex.copy(exchangeName = Expr(exchange), vHost = Expr(vHost)))

  }

  def forQueue(queue : String, vHost : String)( ruleBuilders : QueueValidationRule[_]* ) : Seq[ValidationRule[RabbitMQSink]] = {
    ruleBuilders.map( ex => ex.copy(queueName = Expr(queue), vhost = Expr(vHost)))
  }

  override def build: ArrayBuffer[ValidationRule[RabbitMQSink]] = rules

}

object RabbitMQValidationRuleBuilder {

  val rules: ArrayBuffer[ValidationRule[RabbitMQSink]] = ArrayBuffer.empty

  private[rabbitmq] def addAll(ruleBuilders: Seq[ValidationRule[RabbitMQSink]]): Seq[ValidationRule[RabbitMQSink]] = {
    rules.++=(ruleBuilders)
  }
}


