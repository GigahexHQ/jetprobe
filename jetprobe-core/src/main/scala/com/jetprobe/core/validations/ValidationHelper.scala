package com.jetprobe.core.validations

/**
  * @author Shad.
  */
object ValidationHelper {

  def getFailureMessage(ruleName : String, actual : Any, expected : Any, sourceName : String, line : Int) : String = {
    s"${ruleName} failed at ${sourceName} : ${line}. Expected = $expected , Actual = $actual"
  }

}
