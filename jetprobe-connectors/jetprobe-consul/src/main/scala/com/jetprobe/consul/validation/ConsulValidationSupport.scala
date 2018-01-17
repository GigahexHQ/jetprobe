package com.jetprobe.consul.validation

import com.jetprobe.consul.ConsulService
import com.jetprobe.consul.model.{ServiceInfo, ServiceQuery}
import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule

/**
  * @author Shad.
  */
trait ConsulValidationSupport {

  def given(serviceQuery: ServiceQuery)(rules : ServiceValidationRule[_]*) : Seq[ValidationRule[ConsulService]] = {
    rules.map(serviceRules => serviceRules.copy(service = serviceQuery.name))
  }

  def checkService[U](expected: U, actual: ServiceInfo => U):
  ServiceValidationRule[U] = ServiceValidationRule(expected, actual)

  def service(name : String) = new ServiceQuery(name)

  def consul(host : String,port : String) : ConsulService = ConsulService(Expr(host),Expr(port))

  implicit object ConsulValidator extends ConsulServiceValidator

}
