package com.jetprobe.core.action

import akka.actor.{ActorRef, PoisonPill, Props}
import com.jetprobe.core.runner.ScenarioManager.ExecuteNext
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.validations.{ValidationExecutor, ValidationRule}

import scala.concurrent.duration._
/**
  * @author Shad.
  */
class Validator[D <: DataSource](rulesBuilder: Seq[ValidationRule[D]], runner: ValidationExecutor[D], sink : D,controller : ActorRef) extends BaseActor {

  override def receive: Receive = {
    case FeedMessage(session, next) =>

      val updatedSession = session.copy(validationResuls = runner.execute(rulesBuilder,sink,session.attributes) ++ session.validationResuls)
      controller ! ExecuteNext(next,updatedSession,false)

  }
}

object Validator {
  def props[D <: DataSource](rules: Seq[ValidationRule[D]], runner: ValidationExecutor[D], sink : D,controller : ActorRef): Props =
    Props(new Validator(rules: Seq[ValidationRule[D]], runner,sink,controller))
}