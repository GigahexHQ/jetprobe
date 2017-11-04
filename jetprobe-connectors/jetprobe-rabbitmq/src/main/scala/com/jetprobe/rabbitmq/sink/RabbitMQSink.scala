package com.jetprobe.rabbitmq.sink


import com.jetprobe.core.generator.Generator
import com.jetprobe.core.parser.{Expr, ExpressionParser}
import com.jetprobe.core.sink.DataSink
import com.rabbitmq.client.{Connection, ConnectionFactory}
import com.rabbitmq.http.client.Client
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
case class RabbitMQSink(host: Expr,
                        vHost: Expr = Expr(),
                        exchange: Expr = Expr(),
                        username: Expr = Expr(RabbitMQSink.defaultUserName),
                        password: Expr = Expr(RabbitMQSink.defaultPassword),
                        routingKey: Expr = Expr("*"),
                        protocol: Expr = Expr("http"),
                        config: Map[String, Any] = Map.empty
                       )
  extends DataSink {

  lazy val parsedConfig : Either[Exception,Map[String,String]] = {
    val exprs = Seq(host,vHost,username,routingKey,protocol,username,password)
    ExpressionParser.parseAll(exprs,config)
  }

  lazy val connection: Option[Connection] = RabbitMQSink.getConnection(host,username,password,vHost,parsedConfig)

  lazy val client : Option[Client] = RabbitMQSink.getHttpClient(host,username,password,config)

  //private lazy val channel = connection.createChannel()

  override def save(records: Generator): Unit = {
    val parsedRoutingKey = ExpressionParser.parse(routingKey.value, config)
    val parsedEx = ExpressionParser.parse(exchange.value,config)
    (connection, parsedRoutingKey,parsedEx) match {
      case (Some(conn), Some(rkey),Some(exchng)) =>
        val channel = conn.createChannel()
        if (!channel.isOpen){
          channel.exchangeDeclare(exchng, "topic", true)
        }
        records.foreach(message => {
          channel.basicPublish(exchng, rkey, null, message.getBytes)
        })
        channel.close()
        channel.abort()
        conn.close()

      case _ =>
        logger.error("Unable to create connection")

    }

  }

}

object RabbitMQSink extends LazyLogging{

  val defaultUserName = "guest"
  val defaultPassword = "guest"
  val defaultProtocol = "http"


  def apply(host: String): RabbitMQSink = {
    RabbitMQSink(Expr(host))
  }

  def getConnection(host: Expr,
                    username: Expr,
                    password: Expr,
                    vHost: Expr,
                    parsedConfig : Either[Exception,Map[String,String]]): Option[Connection] = {

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

  def getHttpClient(host : Expr,username : Expr,password : Expr,config : Map[String,Any]) : Option[Client] = {
    val expressions = Seq(host, username, password)
    logger.info(s"parsing host value : ${host.value}")
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
