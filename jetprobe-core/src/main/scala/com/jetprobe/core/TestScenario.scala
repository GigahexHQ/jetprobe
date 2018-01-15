package com.jetprobe.core


import java.util.function.Consumer

import com.jetprobe.core.structure.{ExecutableScenario, ScenarioBuilder}
import com.jetprobe.core.validations.{Passed, ValidationResult}
import org.json4s.DefaultFormats

/**
  * @author Shad.
  */
trait TestScenario extends CoreDsl{

  implicit val formats = DefaultFormats

  /**
    * Define the list of action that needs to be executed as part of the test suite
    * @return The Scenario consisting of list of actions
    */
  def actions : ScenarioBuilder

  def assertEquals[T](expected : T,actual : sourcecode.Text[T])(implicit line: sourcecode.Line, fullName: sourcecode.FullName) : ValidationResult = {
    if(expected == actual.value){
      ValidationResult("sample",Passed)
    }

    else{
      throw new Exception(s"Expression : ${actual.source} at ${fullName.value}:${line.value} evaulate as : ${actual.value}, but expected : ${expected}")
    }

  }

}

abstract class JTestScenario extends TestScenario {



}