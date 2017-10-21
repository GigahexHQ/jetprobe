package com.jetprobe.core.action.builder

import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator
import com.jetprobe.core.action.{Action, Validate, Validator}
import com.jetprobe.core.sink.{DataSink, DataSource}
import com.jetprobe.core.structure.ScenarioContext
import com.jetprobe.core.validations.{ValidationExecutor, ValidationRule, ValidationRulesBuilder}

/**
  * @author Shad.
  */
class ValidationBuilder[D <: DataSource](sink: D, rules: Seq[ValidationRule[D]])(implicit executor : ValidationExecutor[D]) extends ActionBuilder {

  /**
    * @param ctx  the test context
    * @param next the action that will be chained with the Action build by this builder
    * @return the resulting action
    */
  override def build(ctx: ScenarioContext, next: Action): Action = {
    val validator = ctx.system.actorOf(Validator.props(rules,executor,sink),"validation-" + new UUIDGenerator().generateId(this).toString)
    new Validate(validator,next)
  }
}
