package com.jetprobe.rabbitmq.sink


import com.jetprobe.core.generator.Generator
import com.jetprobe.core.sink.DataSink
import com.rabbitmq.client.{Connection, ConnectionFactory}

/**
  * @author Shad.
  */
class RabbitMQSink (val host: String,
                   val vHost: Option[String] = None,
                   val exchange: String = "",
                   val username: String = RabbitMQSink.defaultUserName,
                   val password: String = RabbitMQSink.defaultPassword,
                   val routingKey: String = "*",
                   val protocol: String = "http")
  extends DataSink {

 lazy val connection: Connection =
    RabbitMQSink.getConnection(host, username, password, vHost)
  private lazy val channel = connection.createChannel()

  override def save(records: Generator): Unit = {
    if (!channel.isOpen)
      channel.exchangeDeclare(exchange, "topic", true)
    records.foreach(message => {
      channel.basicPublish(exchange, routingKey, null, message.getBytes)
    })
    channel.close()
    channel.abort()
    connection.close()
  }

}

object RabbitMQSink {

  val defaultUserName = "guest"
  val defaultPassword = "guest"
  val defaultProtocol = "http"


  def apply(host: String,
            protocol: String = defaultProtocol,
            username: String = defaultUserName,
            password: String = defaultPassword): RabbitMQSink = {
    new RabbitMQSink(host,username = username,password = password,protocol = protocol)
  }

  def getConnection(host: String,
                    username: String,
                    password: String,
                    vHost: Option[String]): Connection = {
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setUsername(username)
    factory.setPassword(password)
    if (vHost.nonEmpty)
      factory.setVirtualHost(vHost.get)
    factory.newConnection()
  }

}
