package com.jetprobe.core


import com.jetprobe.core.structure.ExecutableScenario
import com.jetprobe.core.validations.{Passed, ValidationResult}
import org.json4s.DefaultFormats

/**
  * @author Shad.
  */
trait TestScenario extends CoreDsl{

  implicit val formats = DefaultFormats

  def buildScenario : ExecutableScenario

  def assertEquals(expected : Any,actual : sourcecode.Text[Any])(implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationResult = {
    if(expected == actual.value){
      println("Test passed.")
      ValidationResult("sample",Passed)
    }

    else{
      println("this failed.")
      throw new Exception(s"Test failed for expression : ${actual.source} at ${fullName.value}:${line.value}. Expected : ${expected}, Actual : ${actual.value}")
    }

  }

}

abstract class JTestScenario extends TestScenario