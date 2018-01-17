package com.jetprobe.rabbitmq.storage


import java.util

import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.Config
import com.rabbitmq.client.{Connection, ConnectionFactory}
import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.domain.ExchangeInfo
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class RabbitMQBroker private[jetprobe](client: Client, connection: Connection)

  extends Storage  {

  override def cleanup: Unit = {
    client.closeConnection("")
    connection.close()
  }

  def usingConnection[T](fn : Connection => T) : Option[T] = {
    val optRes = Try {
      fn(connection)
    }
    optRes match {
      case Success(result) => Some(result)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        None
    }
  }


  def usingAdmin[T](fn: Client => T): Option[T] = {
    val optRes = Try {
      fn(client)
    }
    optRes match {
      case Success(result) => Some(result)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        None
    }
  }

  def createVirtualHost(vhost: String): Unit = {
    usingAdmin(c => c.createVhost(vhost))
  }

  def createExchange(name: String, vhost: String, exhangeType: String, durable: Boolean, autoDelete: Boolean, internal: Boolean): Unit = {
    usingAdmin(c => c.declareExchange(vhost, name, new ExchangeInfo(exhangeType, durable, autoDelete, internal, new util.TreeMap())))
  }

}

object RabbitMQBroker extends LazyLogging {

  val defaultUserName = "guest"
  val defaultPassword = "guest"
  val defaultProtocol = "http"


  def getConnection(host: Expr,
                    username: Expr,
                    password: Expr,
                    vHost: Expr,
                    parsedConfig: Either[Exception, Map[String, String]]): Option[Connection] = {

    parsedConfig match {
      case Left(ex) =>
        logger.error(ex.getMessage)
        None
      case Right(extractedVals) =>
        val factory = new ConnectionFactory()
        factory.setHost(extractedVals(host.value))
        factory.setUsername(extractedVals(username.value))
        factory.setPassword(extractedVals(host.value))
        if (vHost.value.isEmpty) {
          factory.setVirtualHost(extractedVals(vHost.value))
        }
        Some(factory.newConnection("jetprobe-rabbit"))

    }

  }

  def getHttpClient(host: Expr, username: Expr, password: Expr, config: Map[String, Any]): Option[Client] = {
    val expressions = Seq(host, username, password)
    val parsedVals = ExpressionParser.parseAll(expressions, config)
    parsedVals match {
      case Left(ex) =>
        logger.error(ex.getMessage)
        None
      case Right(extractedVals) =>
        logger.info(s"Client for RabbitMQ Admin : ${extractedVals(host.value)}")
        val client = new Client("http://" + extractedVals(host.value) + ":15672/api/", extractedVals(username.value), extractedVals(password.value))
        Some(client)
    }
  }


}

class RabbitMQConfig(host: String, port: String = "5672", httpPort: String = "15672", username: String = "guest", password: String = "guest")
  extends Config[RabbitMQBroker] {

  /**
    * An impure function to build the storage
    *
    * @param sessionConf
    * @return
    */
  override private[jetprobe] def getStorage(sessionConf: Map[String, Any]): RabbitMQBroker = {

    val exprs = Seq(Expr(host), Expr(username), Expr(password), Expr(httpPort),Expr(port))
    val parsedVals = ExpressionParser.parseAll(exprs, sessionConf)
    parsedVals match {
      case Left(ex) =>
        throw ex
      case Right(values) =>
        val client = getHttpClient(values(host), values(httpPort).toInt, values(username), values(password))
        val connection = getConnection(values(host), values(port).toInt, values(username), values(password))
        new RabbitMQBroker(client, connection)
    }
  }

  private[this] def getHttpClient(host: String, httpPort: Int, username: String, password: String): Client = {
    new Client("http://" + host + ":" + httpPort + "/api/", username, password)
  }

  private[this] def getConnection(host: String, port: Int, username: String, password: String): Connection = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setUsername(username)
    factory.setPassword(password)
    factory.newConnection("jetprobe-rabbit")
  }


}
