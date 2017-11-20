package com.jetprobe.consul.validation

import org.scalatest.FlatSpec

/**
  * @author Shad.
  */
class ConsulValidatorSpec extends FlatSpec with ConsulValidationSupport{

  behavior of "consul validator"

  it should "fetch from consul " in {

    val consulValidationR = given(service("metadata"))(
      checkService[Int](9000,_.port)
    )

    val consulValidator = new ConsulServiceValidator
    val results = consulValidator.execute(consulValidationR,consul("${consul.host}","8500"),Map("consul.host" -> "192.168.1.7"))
    assert(results.size == 1)


  }

}
