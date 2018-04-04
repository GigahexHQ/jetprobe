package com.jetprobe.core.task

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import com.jetprobe.core.session.Session
import com.typesafe.scalalogging.StrictLogging

/**
  * @author Shad.
  */
trait Task extends StrictLogging {

  def name: String

  def !(session: Session): Unit = execute(session)

  def execute(session: Session): Unit
}

trait TaskMessage {

  def name: String

}

case class ForwardedMessage(message: TaskMessage, session: Session)

class SelfExecutableTask(val name : String, val message: TaskMessage, next: Task, actorSystem: ActorSystem,scenarioManager: ActorRef)
                  (fn: (TaskMessage, Session) => Session) extends Task {


  override def execute(session: Session): Unit = {
    val msg = ForwardedMessage(message, session)

    val taskActorRef = actorSystem.actorOf(SelfExecutableTask.props(next,scenarioManager,fn), name + UUID.randomUUID().toString)
    taskActorRef ! msg
    
  }


}

object SelfExecutableTask {

  def props(next : Task, scenarioManager : ActorRef, fn : (TaskMessage, Session) => Session) : Props = Props(new TaskBackedActor(next,scenarioManager) {

    override def execute(taskMessage: TaskMessage, session: Session): Session = {
      try{
        fn(taskMessage, session)
      }catch {
        case ex : Exception => logger.error(ex.getMessage)
          session
      }

    }

  })
}