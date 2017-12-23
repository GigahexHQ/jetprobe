package com.jetprobe.hbase.validation

import com.jetprobe.core.validations.{RuleValidator, ValidationExecutor, ValidationResult, ValidationRule}
import com.jetprobe.hbase.sink.HBaseSink

/**
  * @author Shad.
  */
class HBaseValidator extends ValidationExecutor[HBaseSink]{

  override def execute(rules: Seq[ValidationRule[HBaseSink]], sink: HBaseSink, config: Map[String, Any]): Seq[ValidationResult] = {

    try {
      val connection = sink.getConnection

      rules.map {
        case rv : HBaseSQLRule[_] => rv.copy(hbaseConn = connection).validate(config)

      }
    } catch {
      case ex : Exception => rules.map(ValidationResult.skipped(_, ex.getMessage))
    }


  }


}


