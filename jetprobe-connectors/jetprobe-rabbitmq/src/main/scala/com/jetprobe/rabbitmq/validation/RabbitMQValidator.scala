package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser}
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
class RabbitMQValidator extends ValidationExecutor[RabbitMQSink] with LazyLogging {

  import RabbitMQValidator._

  override def execute(rules: Seq[ValidationRule[RabbitMQSink]], sink: RabbitMQSink, config: Map[String, Any]): Seq[ValidationResult] = {

    try {
      val groupedExchangeRules = rules.filter(rule => rule.isInstanceOf[ExchangeValidationRule[_]]).groupBy {
        case r: ExchangeValidationRule[_] => (r.vHost, r.exchangeName)
      }

      val groupedQueueRules = rules.filter(rule => rule.isInstanceOf[QueueValidationRule[_]]).groupBy {
        case r: QueueValidationRule[_] => (r.vhost, r.queueName)
      }
      sink.copy(config = config).client match {
        case Some(client) =>
          val queueInfos = groupedQueueRules.map {
            case ((vhost, queue), _) => (vhost.value, queue.value) -> getQueueProps(client, vhost, queue, config)
          }

          val exchangeInfos = groupedExchangeRules.map {
            case ((vhost, exchange), _) =>
              (vhost.value, exchange.value) -> getExchangeProps(client, exchange, vhost, config)
          }
          val fresult = executeValidations(rules, exchangeInfos, queueInfos)
          Await.result(fresult, 10.seconds)
        case None =>
          throw new Exception("Unable to build http client for RabbitMQ")
      }
    } catch {
      case ex: Exception => rules.map(x => ValidationResult.skipped(x, ex.getMessage))
    }

  }
}

object RabbitMQValidator extends LazyLogging {

  type Props[T] = Map[(String, String), Either[Exception, T]]

  def executeValidations(rules: Seq[ValidationRule[RabbitMQSink]],
                         exchangeProps: Props[ExchangeProps],
                         queueProps: Props[QueueProps]): Future[Seq[ValidationResult]] = {
    val validationResults = rules.map {
      //Validation for the Exchange
      case r: ExchangeValidationRule[_] =>
        val exchangeInfo = exchangeProps(r.vHost.value, r.exchangeName.value)
        runExchangeValidation(exchangeInfo, r)
      //Validation for the Queue
      case r: QueueValidationRule[_] =>
        runQueueValidation(queueProps(r.vhost.value, r.queueName.value), r)
    }
    Future.sequence(validationResults)

  }

  def getExchangeProps(client: Client, exchange: Expr, vHost: Expr, config: Map[String, Any]): Either[Exception, ExchangeProps] = {
    logger.info("fetching the exchange props")
    try {
      val exprs = Seq(exchange, vHost)
      ExpressionParser.parseAll(exprs, config) match {
        case Left(ex) => throw ex
        case Right(parsedVals) =>
          val parsedExchange = parsedVals(exchange.value)
          val parsedVHost = parsedVals(vHost.value)

          val exchangInfo = client.getExchange(parsedVHost, parsedExchange)
          val exchangeProps = ExchangeProps(exchangInfo.getName, exchangInfo.getType, exchangInfo.isDurable, exchangInfo.isAutoDelete)

          val bindings = client.getBindingsBySource(parsedVHost, parsedExchange).asScala.map(bindingInfo => {
            QueueBinding(bindingInfo.getDestination, bindingInfo.getRoutingKey, bindingInfo.getArguments.asScala.toMap)

          })
          Right(exchangeProps.copy(bindings = bindings))
      }

    } catch {
      case ex: Exception =>
        logger.error(s"Exception occurred : ${ex.getMessage}")
        Left(ex)
    }

  }

  def runExchangeValidation(exchangeProps: Either[Exception, ExchangeProps], rule: ExchangeValidationRule[_])
                           (implicit name: sourcecode.Name, args: sourcecode.Args): Future[ValidationResult] = {
    Future {
      ValidationExecutor.validate[ExchangeProps](exchangeProps, rule) {
        case (mayBeProps, r) =>
          val ruleImpl = r.asInstanceOf[ExchangeValidationRule[_]]
          if (ruleImpl.expected == ruleImpl.actual(mayBeProps)) {
            //println(name.value + args.value.map(_.map(a => a.source + "=" + a.value).mkString("(", ", ", ")")).mkString(""))
            ValidationResult.success(ruleImpl)
          }
          else {
            //println(name.value + args.value.map(_.map(a => a.source + "=" + a.value).mkString("(", ", ", ")")).mkString(""))
            ValidationResult.failed(ruleImpl, ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected))
          }

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
            ValidationResult.failed(ruleImpl, ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected))


      }
    }

  }

  def getQueueProps(client: Client, vHost: Expr, queue: Expr, config: Map[String, Any]): Either[Exception, QueueProps] = {

    try {
      val exprs = Seq(queue, vHost)
      ExpressionParser.parseAll(exprs, config) match {
        case Left(ex) => throw ex
        case Right(parsedVals) =>
          val parsedQueue = parsedVals(queue.value)
          val parsedVhost = parsedVals(vHost.value)
          val queueInfo = client.getQueue(parsedVhost, parsedQueue)
          if (queueInfo == null) {
            throw new Exception(s"Queue = ${parsedQueue} at vHost = ${parsedVhost} not found. Verify the configurations before proceeding.")
          }
          Right(QueueProps(queueInfo.getName, queueInfo.isDurable, queueInfo.isAutoDelete))
      }
    }
    catch {
      case ex: Exception => Left(ex)
    }


  }

}
