package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.{PropertyValidation, ValidationBuilder}
import com.jetprobe.core.storage.{DataSource, Storage, StorageQuery}
import com.jetprobe.core.validations._

/**
  * @author Shad.
  */
trait Validations[B] extends Execs[B]{


  /**
    *
    * @param config Configuration to connect to the Storage
    * @param fnRuleBuilder Function that takes Storage as a parameter and uses the given constructor to build Validation Rule
    * @tparam S The Storage Type
    * @return The Validation rule
    */
  def validateWith[S <: Storage](config: Config[S])(fnRuleBuilder : S => ValidationRule[S]) = {
    val storage = config.getStorage
    exec(new ValidationBuilder[S](storage,Seq(fnRuleBuilder(storage))))
  }




  def given[D,S <: Storage](query : StorageQuery[S,D])(fnRule : Seq[D] => Any)
                           (implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationRule[S] = query.build(fnRule,line,fullName)

  def given[D,S <: Storage](property : D)(fnRule : D => Any)
                           (implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationRule[S] =
    new PropertyBasedValidtation[D,S](property,fnRule,line,fullName)

  def given[D,S <: Storage](property : Option[D])(fnRule : D => Any)
                           (implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationRule[S] =
    new MayBePropertyValidation[D,S](property,fnRule,line,fullName)



}



