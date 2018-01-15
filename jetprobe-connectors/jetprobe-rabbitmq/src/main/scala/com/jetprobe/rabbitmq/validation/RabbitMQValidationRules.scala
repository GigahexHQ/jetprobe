package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.validations.ValidationExecutor.Parsed
import com.jetprobe.core.validations.{RuleValidator, ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.rabbitmq.model._
import com.jetprobe.rabbitmq.storage.RabbitMQBroker
import com.rabbitmq.http.client.Client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author Shad.
  */

trait RabbitMQPropertyFetcher[T] {

  def fetch(mayBeClient: Option[Client]): Parsed[T]
}
/*
case class ExchangeValidationRule(fnAssertion: (ExchangeProps) => Any,
                                  vHost: Expr = Expr(),
                                  exchangeName: Expr = Expr())
  extends ValidationRule[RabbitMQBroker]
    with RabbitMQPropertyFetcher[ExchangeProps]
    with RuleValidator {

  override def name: String = s"RabbitMQ validation for exchange : ${exchangeName.value} in vhost : ${vHost.value}"

  override def fetch(mayBeClient: Option[Client]): Parsed[ExchangeProps] = {

    val result = try {
      val client = mayBeClient match {
        case Some(cl) => cl
        case None => throw new Exception("Unable to fetch the client.")
      }
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

  override def validate(config: Map[String, Any], storage: RabbitMQBroker): ValidationResult = {
    val parsedVals = ExpressionParser.parseAll(Seq(vHost, exchangeName), config)
    parsedVals match {
      case Left(exception) => ValidationResult.failed(this, exception.getMessage)
      case Right(props) =>
        val parsedRule = copy(exchangeName = Expr(props(exchangeName.value)), vHost = Expr(props(vHost.value)))
        val exchangeProps = parsedRule.fetch(null)
        validateResponse[ExchangeProps](exchangeProps, fnAssertion,this)
    }
  }
}*/

/*
case class QueueValidationRule(fnAssertion: (QueueProps) => Any,
                               vhost: Expr = Expr(),
                               queueName: Expr = Expr(),
                               name: String = "") extends ValidationRule[RabbitMQBroker]
  with RabbitMQPropertyFetcher[QueueProps]
  with RuleValidator {

  //def name = s"Validation for Queue : ${queueName.value} in virtual host : ${vhost.value}"
  override def fetch(mayBeClient: Option[Client]): Parsed[QueueProps] = {

    val result = try {
      val client = mayBeClient match {
        case Some(cl) => cl
        case None => throw new Exception("Unable to fetch the client.")
      }
      val queueInfo = client.getQueue(vhost.value, queueName.value)
      if (queueInfo == null) {
        throw new Exception(s"Given Queue = ${queueName.value} and vHost = ${vhost.value} not found. Verify the configurations before proceeding.")
      }
      Right(QueueProps(queueInfo.getName, vhost.value, queueInfo.isDurable, queueInfo.isAutoDelete))

    } catch {
      case ex: Exception => Left(ex)
    }

    Future(result)
  }

  /**
    * Validates Queue properties
    *
    * @param config
    * @param storage
    * @return
    */
  override def validate(config: Map[String, Any], storage: RabbitMQBroker): ValidationResult = {

    val parsedVals = ExpressionParser.parseAll(Seq(vhost, queueName), config)
    parsedVals match {
      case Left(exception) => ValidationResult.failed(this, exception.getMessage)
      case Right(props) =>
        val parsedRule = copy(queueName = Expr(props(queueName.value)), vhost = Expr(props(vhost.value)))
        val queueProps = parsedRule.fetch(null)
        validateResponse[QueueProps](queueProps, fnAssertion,this)
    }

  }
}
*/

/*
trait RabbitMQPredicates {

  def havingExchange(exchange: String, vHost: String): ExchangeQuery = new ExchangeQuery(exchange, vHost)

  def havingQueue(name: String, vHost: String): QueueQuery = new QueueQuery(name, vHost)

}*/
