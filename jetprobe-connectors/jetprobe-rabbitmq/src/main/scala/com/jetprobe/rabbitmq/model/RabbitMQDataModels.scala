package com.jetprobe.rabbitmq.model

import com.jetprobe.core.sink.DataSource

/**
  * @author Shad.
  */
case class ExchangeProps(name : String,
                         exchangeType : String,
                         durable : Boolean,
                         isAutoDelete : Boolean,
                         bindings : Seq[QueueBinding] = Seq.empty
                        ) extends DataSource

case class QueueBinding(to : String,
                        routingKey : String,
                        arguments : Map[String,Any]
                       )

case class QueueProps(name : String,
                      durable : Boolean,
                      autoDelete : Boolean) extends DataSource