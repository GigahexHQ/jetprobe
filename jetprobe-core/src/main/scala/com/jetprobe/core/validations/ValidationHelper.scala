package com.jetprobe.core.validations

/**
  * @author Shad.
  */
object ValidationHelper {

  def getFailureMessage(ruleName : String, actual : Any, expected : Any) : String = {
    s"${ruleName} failed. Expected = $expected , Actual = $actual"
  }

}
