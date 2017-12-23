package com.jetprobe.rabbitmq.validation

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.rabbitmq.model.{ExchangeProps, QueueBinding, QueueProps}
import com.jetprobe.rabbitmq.sink.RabbitMQSink
import com.rabbitmq.http.client.Client
import com.typesafe.scalalogging.LazyLogging


/**
  * @author Shad.
  */
class RabbitMQValidator extends ValidationExecutor[RabbitMQSink] with LazyLogging {


  private[this] def handleExchangeRule(rule : ExchangeValidationRule,client : Client,config : Map[String,Any]) : ValidationResult = {
      val parsedVals = ExpressionParser.parseAll(Seq(rule.vHost,rule.exchangeName),config)
      parsedVals match {
        case Left(exception) => ValidationResult.failed(rule, exception.getMessage)
        case Right(props) =>
          val parsedRule = rule.copy(exchangeName = Expr(props(rule.exchangeName.value)),vHost = Expr(props(rule.vHost.value)))
          val exchangeProps = parsedRule.fetch(client)
          validateResponse[ExchangeProps](exchangeProps,rule.fnAssertion)
      }
  }

  def handleQueueRule(rule: QueueValidationRule, client: Client, config: Map[String, Any]): ValidationResult = {
    val parsedVals = ExpressionParser.parseAll(Seq(rule.vhost,rule.queueName),config)
    parsedVals match {
      case Left(exception) => ValidationResult.failed(rule, exception.getMessage)
      case Right(props) =>
        val parsedRule = rule.copy(queueName = Expr(props(rule.queueName.value)),vhost = Expr(props(rule.vhost.value)))
        val queueProps = parsedRule.fetch(client)
        validateResponse[QueueProps](queueProps,rule.fnAssertion)
    }


  }

  override def execute(rules: Seq[ValidationRule[RabbitMQSink]], sink: RabbitMQSink, config: Map[String, Any]): Seq[ValidationResult] = {
    try {

      sink.copy(config = config).client match {
        case Some(rabbit) =>
          rules.map {
            case rule : ExchangeValidationRule => handleExchangeRule(rule,rabbit,config)
            case rule : QueueValidationRule => handleQueueRule(rule,rabbit,config)

          }
        case None =>
          throw new Exception("Unable to build http client for RabbitMQ")

      }
    }catch {
      case ex: Exception => rules.map(rule => ValidationResult.skipped(rule, ex.getMessage))
    }

    /*try {
      val groupedExchangeRules = rules.filter(rule => rule.isInstanceOf[ExchangeValidationRule[_]]).groupBy {
        case r: ExchangeValidationRule => (r.vHost, r.exchangeName)
      }

      val groupedQueueRules = rules.filter(rule => rule.isInstanceOf[QueueValidationRule[_]]).groupBy {
        case r: QueueValidationRule => (r.vhost, r.queueName)
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
      case ex: Exception => rules.map(x => ValidationResult.skipped(getRuleWithName(x,config), ex.getMessage))
    }
*/
  }
}

object RabbitMQValidator extends LazyLogging {

  import ExpressionParser.parse
  var _config : Map[String,Any] = Map.empty

  type Props[T] = Map[(String, String), Either[Exception, T]]

  /*private[rabbitmq] def getRuleWithName(rule : ValidationRule[RabbitMQSink],config : Map[String,Any]) : ValidationRule[RabbitMQSink] = rule match {
    case rule : ExchangeValidationRule =>
      val ruleName = s"Exchange : ${parse(rule.exchangeName.value,config).getOrElse(rule.exchangeName.value)} " +
        s"in vhost : ${parse(rule.vHost.value,config).getOrElse(rule.vHost.value)} "
      rule.copy(name = ruleName)

    case rule : QueueValidationRule =>
      val ruleName = s"Queue : ${parse(rule.queueName.value,config).getOrElse(rule.queueName.value)} " +
        s"in vhost : ${parse(rule.vhost.value,config).getOrElse(rule.vhost.value)} "
      rule.copy(name = ruleName)
  }*/

  /*def executeValidations(rules: Seq[ValidationRule[RabbitMQSink]],
                         exchangeProps: Props[ExchangeProps],
                         queueProps: Props[QueueProps]): Future[Seq[ValidationResult]] = {
    val validationResults = rules.map {
      //Validation for the Exchange
      case r: ExchangeValidationRule =>
        val exchangeInfo = exchangeProps(r.vHost.value, r.exchangeName.value)
        runExchangeValidation(exchangeInfo, getRuleWithName(r,_config).asInstanceOf[ExchangeValidationRule[_]])
      //Validation for the Queue
      case r: QueueValidationRule =>
        runQueueValidation(queueProps(r.vhost.value, r.queueName.value), getRuleWithName(r,_config).asInstanceOf[QueueValidationRule[_]])
    }
    Future.sequence(validationResults)

  }*/

  /*def getExchangeProps(client: Client, exchange: Expr, vHost: Expr, config: Map[String, Any]): Either[Exception, ExchangeProps] = {

    try {
      _config = config
      val exprs = Seq(exchange, vHost)
      ExpressionParser.parseAll(exprs, _config) match {
        case Left(ex) => throw ex
        case Right(parsedVals) =>
          val parsedExchange = parsedVals(exchange.value)
          val parsedVHost = parsedVals(vHost.value)

          val exchangInfo = client.getExchange(parsedVHost, parsedExchange)
          if (exchangInfo == null) {
            throw new Exception(s"Given Exchange = ${parsedExchange} and vHost = ${parsedVHost} not found. Verify the configurations before proceeding.")
          }

          val exchangeProps = ExchangeProps(exchangInfo.getName,parsedVHost, exchangInfo.getType, exchangInfo.isDurable, exchangInfo.isAutoDelete)
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
                           : Future[ValidationResult] = {
    Future {
      ValidationExecutor.validate[ExchangeProps](exchangeProps, rule) {
        case (mayBeProps, r) =>
          val ruleImpl = r.asInstanceOf[ExchangeValidationRule[_]]
          if (ruleImpl.expected == ruleImpl.actual(mayBeProps)) {
            ValidationResult.success(ruleImpl)
          }
          else {
            val condition = s"Exchange : ${mayBeProps.name} in vHost : ${mayBeProps.vHost}"
            ValidationResult.failed(ruleImpl.copy(name = condition), ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected,condition))
          }

      }
    }

  }*/

 /* def runQueueValidation(queueProps: Either[Exception, QueueProps], rule: QueueValidationRule[_]): Future[ValidationResult] = {

    Future {
      ValidationExecutor.validate[QueueProps](queueProps, rule) {
        case (mayBeProps, r) =>
          val ruleImpl = r.asInstanceOf[QueueValidationRule[_]]
          if (ruleImpl.expected == ruleImpl.actual(mayBeProps))
            ValidationResult.success(ruleImpl)
          else{

            val condition = s"Queue : ${mayBeProps.name} in vHost : ${mayBeProps.vHost}"
            ValidationResult.failed(ruleImpl.copy(name = condition), ruleImpl.onFailure(ruleImpl.actual(mayBeProps), ruleImpl.expected,condition))
          }



      }
    }

  }

  def getQueueProps(client: Client, vHost: Expr, queue: Expr, config: Map[String, Any]): Either[Exception, QueueProps] = {

    try {
      _config = config
      val exprs = Seq(queue, vHost)
      ExpressionParser.parseAll(exprs, config) match {
        case Left(ex) => throw ex
        case Right(parsedVals) =>
          val parsedQueue = parsedVals(queue.value)
          val parsedVhost = parsedVals(vHost.value)
          val queueInfo = client.getQueue(parsedVhost, parsedQueue)
          if (queueInfo == null) {
            throw new Exception(s"Given Queue = ${parsedQueue} and vHost = ${parsedVhost} not found. Verify the configurations before proceeding.")
          }
          Right(QueueProps(queueInfo.getName,parsedVhost, queueInfo.isDurable, queueInfo.isAutoDelete))
      }
    }
    catch {
      case ex: Exception => Left(ex)
    }


  }*/

}
