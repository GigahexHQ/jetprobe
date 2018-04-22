package com.jetprobe.sample

import java.util

import com.jetprobe.core.TestPipeline
import com.jetprobe.core.annotation.{PipelineMeta, TestSuite}
import com.jetprobe.core.fs.{FilePath, FileStorage}
import com.jetprobe.core.structure.PipelineBuilder
import com.jetprobe.rabbitmq.storage.RabbitMQConfig

import scala.collection.JavaConverters._

/**
  * @author Shad.
  */
@PipelineMeta(name = "RabbitMQ test suite")
class RabbitMQSuite extends TestPipeline {

  override def tasks: PipelineBuilder = {

    val rabbitConf = new RabbitMQConfig("xxx.xx.xx.xx")

    val fs = new FilePath("""/path/to/file""")

    task("Create virtual host",rabbitConf) { rabbit =>

      rabbit.createVirtualHost("fancyvhost")

    }

    task("Create user",rabbitConf) { rabbit =>

      rabbit.usingAdmin { client =>

        client.createUser("username", "withPassword".toCharArray, util.Arrays.asList("moderator"))

      }

    }

    task("Read and copy",fs) { file =>

      file.lines().foreach(println)

      file.copyTo("/path/to/destination")

    }

    validate("Check file content",fs) { file =>

      val res = file.usingFile(f => f.getAbsolutePath)
      given(res)(st => assertEquals(true, st.contains("rules10")))
    }

    validate("validate the exchanges",rabbitConf) { rabbit =>

      val listofExchanges = rabbit.usingAdmin(admin => admin.getExchanges)

      given(listofExchanges) { exchanges =>
        val exchangNames = exchanges.asScala.map(_.getName)

        assertEquals(true, exchangNames.contains("amq.topic"))
        assertEquals("amq.headers", exchangNames.find(_.contains("headers")).get)

      }
    }

    validate("Check amq.topic",rabbitConf) { rabbit =>

      val topicExchange = rabbit.usingAdmin(admin => admin.getExchange("/", "amq.topic"))

      given(topicExchange) { exchangInfo =>

        assertEquals(true, exchangInfo.isAutoDelete)
        assertEquals(false, exchangInfo.isInternal)
        assertEquals(true, exchangInfo.isDurable)

      }
    }

  }
}
