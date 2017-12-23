package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationExecutor.Parsed
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.rabbitmq.model._
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.jetprobe.rabbitmq.validation.RabbitMQValidator.logger
import com.rabbitmq.http.client.Client
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author Shad.
  */

trait RabbitMQPropertyFetcher[T] {

  def fetch(client : Client) : Parsed[T]
}

case class ExchangeValidationRule(fnAssertion: (ExchangeProps) => Any,
                                            vHost: Expr = Expr(),
                                            exchangeName: Expr = Expr())
                                extends ValidationRule[RabbitMQSink] with RabbitMQPropertyFetcher[ExchangeProps] {

  override def name: String = s"RabbitMQ validation for exchange : ${exchangeName.value} in vhost : ${vHost.value}"

  override def fetch(client: Client): Parsed[ExchangeProps] = {

   val result = try {
      val exchangInfo = client.getExchange(vHost.value, exchangeName.value)
      if (exchangInfo == null) {
        throw new Exception(s"Given Exchange = ${exchangeName.value} and vHost = ${vHost.value} not found. Verify the configurations before proceeding.")
      }

      val exchangeProps = ExchangeProps(exchangInfo.getName, vHost.value, exchangInfo.getType, exchangInfo.isDurable, exchangInfo.isAutoDelete)
      val bindings = client.getBindingsBySource(vHost.value, exchangeName.value).asScala.map(bindingInfo => {
        QueueBinding(bindingInfo.getDestination, bindingInfo.getRoutingKey, bindingInfo.getArguments.asScala.toMap)

      })
      Right(exchangeProps.copy(bindings = bindings))
    } catch {
      case ex: Exception =>

        Left(ex)
    }

    Future(result)
  }

}

case class QueueValidationRule(fnAssertion: (QueueProps) => Any,
                                         vhost: Expr = Expr(),
                                         queueName: Expr = Expr(),
                                         name: String = "") extends ValidationRule[RabbitMQSink] with RabbitMQPropertyFetcher[QueueProps]{

  //def name = s"Validation for Queue : ${queueName.value} in virtual host : ${vhost.value}"
  override def fetch(client: Client): Parsed[QueueProps] = {

   val result =  try{
      val queueInfo = client.getQueue(vhost.value, queueName.value)
      if (queueInfo == null) {
        throw new Exception(s"Given Queue = ${queueName.value} and vHost = ${vhost.value} not found. Verify the configurations before proceeding.")
      }
      Right(QueueProps(queueInfo.getName,vhost.value, queueInfo.isDurable, queueInfo.isAutoDelete))

    } catch {
      case ex : Exception => Left(ex)
    }

    Future(result)
  }
}

trait RabbitMQPredicates {

  def havingExchange(exchange : String,vHost : String) : ExchangeQuery = new ExchangeQuery(exchange,vHost)
  def havingQueue(name : String, vHost : String) : QueueQuery = new QueueQuery(name,vHost)

}