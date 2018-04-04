package com.jetprobe.core.task.builder

import com.jetprobe.core.Predef.Session
import com.jetprobe.core.task._
import com.jetprobe.core.storage.Storage
import com.jetprobe.core.structure.{Config, PipelineContext}
import com.jetprobe.core.validations.{ValidationResult, ValidationRule}

import scala.util.{Failure, Success, Try}

/**
  * @author Shad.
  */
class ValidationBuilder[S <: Storage](storeConfig: Config[S], rulesBuilder: S => ValidationRule[S]) extends TaskBuilder {

  val name: String = "ValidationTask"

  /**
    * @param ctx  the test context
    * @param next the task that will be chained with the Task build by this builder
    * @return the resulting task
    */
  override def build(ctx: PipelineContext, next: Task): Task = {
    new SelfExecutableTask(name, ValidationMessage(storeConfig, rulesBuilder, name), next, ctx.system, ctx.controller)(runValidator)
  }

  private[this] def runValidator(message: TaskMessage, session: Session): Session = {

    message match {
      case msg: ValidationMessage[S] =>
        val storage = msg.storeConfig.getStorage(session.attributes)
        val result = msg.getRule(session.attributes).validate(session.attributes, storage)
        session.copy(validationResults = session.validationResults ++ Seq(result))
    }

  }
}


case class ValidationMessage[S <: Storage](storeConfig: Config[S], rulesBuilder: S => ValidationRule[S], name: String) extends TaskMessage {

  def getRule(config: Map[String, Any]): ValidationRule[S] = {
    val storage = storeConfig.getStorage(config)
    rulesBuilder(storage)
  }

}

class RegisterValidation[S <: Storage](storeConfig: Config[S], description: String, fnTest: S => Any) extends TaskBuilder {

  override def build(ctx: PipelineContext, next: Task): Task = {

    val message = ValidateStorage(storeConfig, fnTest, description)

    new SelfExecutableTask(description.replaceAll(" ", "-"), message, next, ctx.system, ctx.controller)(runValidator)

  }

  private[this] def runValidator(message: TaskMessage, session: Session): Session = {

    message match {
      case msg: ValidateStorage[S] =>
        val result = Try(msg.storeConfig.getStorage(session.attributes)) match {
          case Success(storage) =>
            Try(msg.fnTest(storage)) match {
              case Success(_) => ValidationResult.success(msg.name)
              case Failure(ex) =>
                ValidationResult.failed(msg.name,ex.getMessage)
            }

          case Failure(ex) =>
            ValidationResult.skipped(msg.name, ex.getMessage)

        }

        session.copy(validationResults = session.validationResults ++ Seq(result))
    }

  }


  case class ValidateStorage[S <: Storage](storeConfig: Config[S], fnTest: S => Any, name: String) extends TaskMessage


}

class PropertyValidation[D](val property: D, val fn: D => Any) extends TaskBuilder {

  val name: String = getClass.getSimpleName

  override def build(ctx: PipelineContext, next: Task): Task = {
    new SelfExecutableTask(name, ValidationRequest(this), next, ctx.system, ctx.controller)(runValidation)
  }

  private[this] def runValidation(message: TaskMessage, session: Session): Session = message match {
    case ValidationRequest(request) =>
      val tryResult = Try(request.fn.apply(request.property))
      tryResult match {
        case Success(_) => session.copy(validationResults = session.validationResults ++ List(ValidationResult.success()))
        case Failure(ex) => session.copy(validationResults = session.validationResults ++ List(ValidationResult.failed(ex.getMessage)))
      }
  }

}

case class ValidationRequest[D](request: PropertyValidation[D]) extends TaskMessage {

  override def name: String = request.name
}
