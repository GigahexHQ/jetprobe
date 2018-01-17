package com.jetprobe.core.action.builder

import com.jetprobe.core.Predef.Session
import com.jetprobe.core.action._
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, ScenarioContext}
import com.jetprobe.core.validations.{ValidationResult, ValidationRule}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class ValidationBuilder[S <: Storage](storeConfig: Config[S], rulesBuilder : S => ValidationRule[S]) extends ActionBuilder {

  val name : String = "ValidationAction"

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    new SelfExecutableAction(name,ValidationMessage(storeConfig,rulesBuilder,name),next,ctx.system,ctx.controller)(runValidator)
  }

  private[this] def runValidator(message: ActionMessage, session: Session) : Session = {

    message match {
      case msg : ValidationMessage[S] =>
        val storage = msg.storeConfig.getStorage(session.attributes)
        val result = msg.getRule(session.attributes).validate(session.attributes,storage)
        session.copy(validationResults = session.validationResults ++ Seq(result))
    }

  }
}


case class ValidationMessage[S <: Storage](storeConfig: Config[S], rulesBuilder : S => ValidationRule[S], name : String) extends ActionMessage {

  def getRule(config : Map[String,Any]) : ValidationRule[S] = {
    val storage = storeConfig.getStorage(config)
    rulesBuilder(storage)
  }

}

class PropertyValidation[D](val property : D,val fn : D => Any) extends ActionBuilder {

  val name : String = getClass.getSimpleName

  override def build(ctx: ScenarioContext, next: Action): Action = {
    new SelfExecutableAction(name,ValidationRequest(this),next,ctx.system,ctx.controller)(runValidation)
  }

  private[this] def runValidation(message: ActionMessage, session: Session) : Session = message match {
    case ValidationRequest(request) =>
      val tryResult = Try(request.fn.apply(request.property))
      tryResult match {
        case Success(_) => session.copy(validationResults = session.validationResults ++ List(ValidationResult.success()))
        case Failure(ex) => session.copy(validationResults = session.validationResults ++ List(ValidationResult.failed(ex.getMessage)))
      }
  }

}

case class ValidationRequest[D](request : PropertyValidation[D]) extends ActionMessage {

  override def name: String = request.name
}
