package com.jetprobe.core.action.builder

import com.jetprobe.core.Predef.Session
import com.jetprobe.core.action._
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.core.validations.{ValidationExecutor, ValidationRule}

/**
  * @author Shad.
  */
class ValidationBuilder[D <: DataSource](sink: D, rules: Seq[ValidationRule[D]])
                                        (implicit executor : ValidationExecutor[D]) extends ActionBuilder {

  val name : String = "ValidationAction"

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    //val validator = ctx.system.actorOf(Validator.props(rules,executor,sink,ctx.controller),"validation-" + new UUIDGenerator().generateId(this).toString)
    //new Validate(validator,next)
    new SelfExecutableAction(name,ValidationMessage(sink,rules,executor,name),next,ctx.system,ctx.controller)(runValidator)
  }

  private[this] def runValidator(message: ActionMessage, session: Session) : Session = {

    message match {
      case ValidationMessage(sink,rules,runner,name) =>
        session.copy(validationResuls = runner.execute(rules,sink,session.attributes) ++ session.validationResuls)
    }

  }
}


case class ValidationMessage[D <: DataSource](sink: D, rules: Seq[ValidationRule[D]],executor : ValidationExecutor[D], name : String) extends ActionMessage