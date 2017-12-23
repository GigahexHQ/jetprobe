package com.jetprobe.consul.validation

import com.ecwid.consul.v1.{ConsulClient, QueryParams}
import com.jetprobe.consul.ConsulService
import com.jetprobe.consul.model.ServiceInfo
import com.jetprobe.core.validations.{ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.core.parser.ExpressionParser._

import scala.util.{Failure, Try}
/**
  * @author Shad.
  */
class ConsulServiceValidator extends ValidationExecutor[ConsulService]{


  override def execute(rules: Seq[ValidationRule[ConsulService]], sink: ConsulService, config: Map[String, Any]): Seq[ValidationResult] = {

    val consulClient = parseAll(Seq(sink.host,sink.port),config)//.map(res => new ConsulClient(res(sink.host.value),res(sink.port.value).toInt))
    consulClient match {
      case Left(exception) =>
        rules.map(r => ValidationResult.skipped(r,exception.getMessage))
      case Right(clientProps) =>
        val client = new ConsulClient(clientProps(sink.host.value),clientProps(sink.port.value).toInt)
        val mayBeService = ConsulServiceValidator.getServiceInfo(rules,client)
        rules.map {
          case vr : ServiceValidationRule[_] => ConsulServiceValidator.validateServiceInfo(vr,mayBeService(vr.service))
        }
        /*rules.map{ vr =>
          case vr : ServiceValidationRule[_] => ConsulServiceValidator.validateServiceInfo(vr,mayBeService(vr.name))
        }*/

    }


  }


}

object ConsulServiceValidator {

  def getServiceInfo(rules: Seq[ValidationRule[ConsulService]] , client : ConsulClient) : Map[String,Either[Exception,ServiceInfo]] = {

    val groupedRules = rules.groupBy(rule => rule.asInstanceOf[ServiceValidationRule[_]].service)
    groupedRules.map {
      case (k,v) =>
       val tryService = try{
          val serviceHealth = client.getHealthServices(k,true,QueryParams.DEFAULT)
          if(serviceHealth.getValue.size() == 0 ) throw new Exception(s"Service ${k} not found")
          else{
            val singleVal = serviceHealth.getValue.get(0)
            Right(ServiceInfo(k,singleVal.getService.getAddress,singleVal.getService.getPort))
          }

        } catch {
         case ex : Exception => Left(ex)
       }

        k -> tryService

    }

  }

  def validateServiceInfo(rule : ServiceValidationRule[_],info : Either[Exception,ServiceInfo]) : ValidationResult = {

    ValidationExecutor.validate[ServiceInfo](info,rule){
      case (sInfo,vRule) =>
        val ruleImpl: ServiceValidationRule[_] = vRule.asInstanceOf[ServiceValidationRule[_]]
        if (ruleImpl.expected == ruleImpl.actual(sInfo)) {
          ValidationResult.success(ruleImpl)
        }
        else {
          val condition = s"Service name : ${ruleImpl.service}"
          ValidationResult.failed(ruleImpl.copy(name = condition), "failed")
        }
    }

  }

}
