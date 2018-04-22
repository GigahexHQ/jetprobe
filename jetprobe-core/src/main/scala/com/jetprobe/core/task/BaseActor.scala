package com.jetprobe.core.task

import akka.actor.{Actor, Terminated}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration

/**
  * @author Shad.
  */

abstract class BaseActor extends Actor with LazyLogging {

  implicit def system = context.system
  def scheduler = system.scheduler
  implicit def dispatcher = system.dispatcher

  def stopChildren : Unit = {
    context.children foreach  { child =>
      context.unwatch(child)
      context.stop(child)
    }
  }


  override def preStart(): Unit = context.setReceiveTimeout(Duration.Undefined)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit =
    logger.error(s"Actor $this crashed on message $message", reason)


  override def unhandled(message: Any): Unit =
    message match {
      case Terminated(dead) => logger.error(s"Unhandled message : ${message}")
      case unknown          => throw new IllegalArgumentException(s"Actor $this doesn't support message $unknown")
    }

  override def postStop(): Unit = logger.info(s"Stopping the actor ${self.path.name}")
}


case object ExecuteCommand