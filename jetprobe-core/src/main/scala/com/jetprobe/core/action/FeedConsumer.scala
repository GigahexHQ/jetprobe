package com.jetprobe.core.action

import akka.actor.Props
import com.jetprobe.core.sink.DataSink
import com.jetprobe.core.generator.Generator

/**
  * @author Shad.
  */
class FeedConsumer(sink: DataSink) extends BaseActor {

  override def receive: Receive = {

    case FeedMessage(session, next) => {
      sink.save(session.records)
      next ! session
    }
  }

}

object FeedConsumer {

  def props(sink : DataSink) : Props = Props(new FeedConsumer(sink))
}


