package com.jetprobe.sample

import com.jetprobe.core.runner.TestBuilder

/**
  * @author Shad.
  */
class SecondScn extends TestBuilder{

  override def testName: String = "Second test here we go again."

  def testCustom(name : String) : String = "name : " + name

}
