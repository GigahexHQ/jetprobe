package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.validations.{ValidationRule, ValidationRulesBuilder}
import com.jetprobe.rabbitmq.sink.RabbitMQSink

import scala.collection.mutable.ArrayBuffer

/**
  * @author Shad.
  */
class RabbitMQValidationRuleBuilder(sink : RabbitMQSink) extends ValidationRulesBuilder[RabbitMQSink]{

  import RabbitMQValidationRuleBuilder._

  val rules: ArrayBuffer[ValidationRule[RabbitMQSink]] = ArrayBuffer.empty

  def forExchange(exchange : String, vHost : String = "/")( rules : ExchangeValidationRule[_]* ) : Seq[ValidationRule[RabbitMQSink]] = {
    val updatedRules = rules.map( ex => ex.copy(exchangeName = exchange, vHost = vHost))
    addAll(updatedRules)
  }

  def forQueue(queue : String, vHost : String)( rules : QueueValidationRule[_]* ) : Seq[ValidationRule[RabbitMQSink]] = {
    val updatedRules = rules.map( ex => ex.copy(queueName = queue, vhost = vHost))
    addAll(updatedRules)
  }



  override def build: ArrayBuffer[ValidationRule[RabbitMQSink]] = rules

  override def context: Map[String, Any] = Map.empty
}

object RabbitMQValidationRuleBuilder {

  val rules: ArrayBuffer[ValidationRule[RabbitMQSink]] = ArrayBuffer.empty

  private[rabbitmq] def addAll(ruleBuilders: Seq[ValidationRule[RabbitMQSink]]): Seq[ValidationRule[RabbitMQSink]] = {
    rules.++=(ruleBuilders)
  }
}


