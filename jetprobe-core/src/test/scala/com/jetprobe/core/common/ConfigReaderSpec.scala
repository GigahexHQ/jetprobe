package com.jetprobe.core.common

import java.util

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Shad.
  */
class ConfigReaderSpec extends FlatSpec with Matchers{


  behavior of "Configuration readers"

  it should("read yaml files") in {

    val yaml = getClass.getResource("/common/test-pipe.yaml").getFile
    val props = YamlConfigReader.fromFile(yaml)

    assert(props.size == 4)

    props("tags") should equal (util.Arrays.asList("fast","test"))

  }


  it should("read properties file") in {

    val propsFile = getClass.getResource("/common/test-prop.properties").getFile
    val props = PropertiesConfigReader.fromFile(propsFile)

    assert(props.size == 3)

    props("project.home") should equal ("/home/name")
  }

}
