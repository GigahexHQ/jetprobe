package com.jetprobe.core.action

import akka.actor.ActorRef
import com.jetprobe.core.session.Session
import com.typesafe.scalalogging.StrictLogging

/**
  * @author Shad.
  */
trait Action extends StrictLogging{

  def name : String
  def !(session : Session) : Unit = execute(session)
  def execute(session : Session) : Unit
}

trait PipedAction extends Action {

  def next : Action

}

class DelegatorAction(val name : String, actor : ActorRef) extends Action {
  override def execute(session: Session): Unit = actor ! session
}
