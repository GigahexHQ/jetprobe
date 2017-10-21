package com.jetprobe.core.action

import akka.actor.Props
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.validations.{ValidationExecutor, ValidationRule, ValidationRulesBuilder}

/**
  * @author Shad.
  */
class Validator[D <: DataSource](rulesBuilder: Seq[ValidationRule[D]], runner: ValidationExecutor[D], sink : D) extends BaseActor {

  override def receive: Receive = {
    case FeedMessage(session, next) =>
      //println(s"executing the validations ${rulesBuilder.build.size}")
      val updatedSession = session.copy(validationResuls = runner.execute(rulesBuilder,sink) ++ session.validationResuls)

      //updatedSession.validationResuls.foreach(println)
      next ! updatedSession
  }
}

object Validator {
  def props[D <: DataSource](rules: Seq[ValidationRule[D]], runner: ValidationExecutor[D], sink : D): Props =
    Props(new Validator(rules: Seq[ValidationRule[D]], runner,sink))
}