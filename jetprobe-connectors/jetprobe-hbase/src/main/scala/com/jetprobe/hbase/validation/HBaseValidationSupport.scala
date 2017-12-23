package com.jetprobe.hbase.validation

import com.jetprobe.core.parser.Expr
import com.jetprobe.core.validations.ValidationRule
import com.jetprobe.hbase.sink.HBaseSink

/**
  * @author Shad.
  */
trait HBaseValidationSupport {

  def given[T](sql : HBaseSQL[T])(ruleFn : Seq[T] => Any) : ValidationRule[HBaseSink] = {
    HBaseSQLRule(ruleFn,sql.query,sql.decoder)
  }

  implicit object HBaseValidationExecutor extends HBaseValidator

}
