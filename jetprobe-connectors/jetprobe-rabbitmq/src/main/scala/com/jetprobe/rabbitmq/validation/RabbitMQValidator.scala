package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.rabbitmq.model.{ExchangeProps, QueueBinding, QueueProps}
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.rabbitmq.http.client.Client
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Shad.
  */
class RabbitMQValidator extends ValidationExecutor[RabbitMQSink] with LazyLogging{

  import RabbitMQValidator._

  override def execute(rules: Seq[ValidationRule[RabbitMQSink]], sink: RabbitMQSink): Seq[ValidationResult] = {

    lazy val client = new Client("http://" + sink.host + ":15672/api/", sink.username, sink.password)
    //println(s"username : ${sink.username} & password : ${sink.password}")

    val groupedExchangeRules = rules.filter(rule => rule.isInstanceOf[ExchangeValidationRule[RabbitMQSink]]).groupBy {
      case r: ExchangeValidationRule[_] => (r.vHost, r.exchangeName)
    }

    val groupedQueueRules = rules.filter(rule => rule.isInstanceOf[QueueValidationRule[_]]).groupBy {
      case r: QueueValidationRule[_] => (r.vhost, r.queueName)
    }

    val exchangeInfos = groupedExchangeRules.map {
      case ((vhost, exchange), _) =>
        (vhost, exchange) -> RabbitMQValidator.getExchangeProps(client, exchange, vhost)
    }

    val queueInfos = groupedQueueRules.map {
      case ((vhost,queue),_) => (vhost,queue) -> RabbitMQValidator.getQueueProps(client,vhost,queue)
    }

    val validationResults = rules.map {

      //Validation for the Exchange
      case r: ExchangeValidationRule[_] =>
        val exchangeProps = exchangeInfos(r.vHost, r.exchangeName)
        runExchangeValidation(exchangeProps, r)
      //Validation for the Queue
      case r: QueueValidationRule[_] =>
        val queueProps = RabbitMQValidator.getQueueProps(client, r.vhost, r.queueName)
        runQueueValidation(queueProps, r)


    }
    val fresult = Future.sequence(validationResults)

    Await.result(fresult, 10.seconds)

  }
}

object RabbitMQValidator extends LazyLogging{


  def getExchangeProps(client: Client, exchange: String, vHost: String): Either[Exception, ExchangeProps] = {
      logger.info("fetching the exchange props")
      try {
        val exchangInfo = client.getExchange(vHost, exchange)
        val exchangeProps = ExchangeProps(exchangInfo.getName, exchangInfo.getType, exchangInfo.isDurable, exchangInfo.isAutoDelete)

        val bindings = client.getBindingsBySource(vHost, exchange).asScala.map(bindingInfo => {
          QueueBinding(bindingInfo.getDestination, bindingInfo.getRoutingKey, bindingInfo.getArguments.asScala.toMap)

        })
        Right(exchangeProps.copy(bindings = bindings))
      } catch {
        case ex: Exception =>
          logger.error(s"Exception occurred while connecting to RabbitMQ Admin Server : ${ex.getMessage}")
          Left(ex)
      }

  }

  def runExchangeValidation(exchangeProps: Either[Exception, ExchangeProps], rule: ExchangeValidationRule[_]): Future[ValidationResult] = {
    Future {
      ValidationExecutor.validate[ExchangeProps](exchangeProps, rule) {
        case (mayBeProps, r) =>
          val ruleImpl = r.asInstanceOf[ExchangeValidationRule[_]]
          if (ruleImpl.expected == ruleImpl.actual(mayBeProps)) {

            ValidationResult.success(ruleImpl)
          }
          else
            ValidationResult(false, None, Some(ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected)))

      }
    }

  }

  def runQueueValidation(queueProps: Either[Exception, QueueProps], rule: QueueValidationRule[_]): Future[ValidationResult] = {

    Future {
      ValidationExecutor.validate[QueueProps](queueProps, rule) {
        case (mayBeProps, r) =>
          val ruleImpl = r.asInstanceOf[QueueValidationRule[_]]
          if (ruleImpl.expected == ruleImpl.actual(mayBeProps))
            ValidationResult.success(ruleImpl)
          else
            ValidationResult(false, None, Some(ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected)))

      }
    }

  }

  def getQueueProps(client: Client, vHost: String, queue: String): Either[Exception, QueueProps] = {

      try {
        val queueInfo = client.getQueue(vHost, queue)
        Right(QueueProps(queueInfo.getName, queueInfo.isDurable, queueInfo.isAutoDelete))
      } catch {
        case ex: Exception => Left(ex)
      }


  }

}
