package com.jetprobe.core.structure

import com.jetprobe.core.task.builder._
import com.jetprobe.core.storage.{DataSource, Storage, StorageQuery}
import com.jetprobe.core.validations._

import scala.concurrent.Future

/**
  * @author Shad.
  */
trait Validations[B] extends Execs[B] {



  def validateGiven[S <: Storage](description: String, config: Config[S])(fnRuleBuilder: S => ValidationRule[S]) = {
    exec(new ValidationBuilder[S](description, config, fnRuleBuilder))
  }

  def validate[S <: Storage](description: String, config: Config[S])(testFn: S => Any) =
    exec(new RegisterValidation[S](description, config, testFn))

  def validate(description : String)(testFn : () => Any) = exec(new BasicValidationBuilder(description,testFn))

  def given[T <: Any](envVars :Map[String,T],key : String)(testFn : T => Any)(implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationRule[Storage] =  {
    val optProperty = envVars.get(key)
    new MayBePropertyValidation[T, Storage](optProperty, testFn, line, fullName)
  }


  def given[D, S <: Storage](query: StorageQuery[S, D])(fnRule: Seq[D] => Any)
                            (implicit line: sourcecode.Line, fullName: sourcecode.FullName): ValidationRule[S] = query.build(fnRule, line, fullName)

  def given[D, S <: Storage](property: D)(fnRule: D => Any)
                            (implicit line: sourcecode.Line, fullName: sourcecode.FullName): ValidationRule[S] =
    new PropertyBasedValidtation[D, S](property, fnRule, line, fullName)

  def given[D, S <: Storage](property: Option[D])(fnRule: D => Any)
                            (implicit line: sourcecode.Line, fullName: sourcecode.FullName): ValidationRule[S] =

    new MayBePropertyValidation[D, S](property, fnRule, line, fullName)

}



