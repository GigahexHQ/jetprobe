package com.jetprobe.rabbitmq.sample

import akka.actor.ActorSystem
import com.jetprobe.core.extractor.JsonPathExtractor._
import com.jetprobe.core.http.Http
import com.jetprobe.core.runner.Runner
import com.jetprobe.core.Predef._
import com.jetprobe.rabbitmq.model.ExchangeProps
import com.jetprobe.rabbitmq.sink.RabbitMQSink

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author Shad.
  */
@Deprecated
object DemoScenarioRun extends App {

  /*type Add = Int => Int

  def add(value : sourcecode.Args)(implicit enclosing: sourcecode.Enclosing) = {
    println(enclosing.value + " [" + value.source + "]: " + value.value)
  }
  def debug[V](value: sourcecode.Text[V])(implicit enclosing: sourcecode.Enclosing) = {
    println(enclosing.value + " [" + value.source + "]: " + value.value)
  }
  class Foo(arg: Int){
    debug(arg) // sourcecode.DebugRun.main Foo [arg]: 123
    def bar(param: String) = {
      debug(100 + 200)
      //add((x:Int) => x+1)
    }
  }
  new Foo(123).bar("lol")

  val dataset = fromTemplate(
    "C:\\Users\\samez\\Documents\\match-service\\sample.txt",
    "C:\\Users\\samez\\Documents\\match-service\\data.csv",
    100)

  val defaultDB = "52ZSTB0IDK6dXxaEQLUaQu"
  val coll = "PfObjects"

  val rabbit = RabbitMQSink("192.168.37.128")


  val pipe = scenario("First Test Scenario")
    //.http(insertPost)
    //.pause(3.seconds)
    // .http(getPosts)
    .validate(rabbit) { rbt =>
    rbt.forExchange(exchange = "history_event_data", vHost = "iORXPuAssgckXVq8B4xcwg")(
     // viewExchange[String]("direct", _.exchangeType),
      checkExchange[Int](1, _.bindings.size),
      checkExchange[Boolean](true, _.bindings.exists(_.to.startsWith("mdm.match-api")))

    )

    rbt.forQueue(queue = "mdm.business-entity.dispatch.api", vHost = "iORXPuAssgckXVq8B4xcwg")(
      checkQueue[Boolean](true, _.autoDelete),
      checkQueue[Boolean](true, _.durable)

    )

  }
    //.pause(3.seconds)

    //Add some more tests
    .build

  implicit val actorSystem = ActorSystem()

 Runner().run(Seq(pipe))
  //Thread.sleep(3000)
  Await.result(actorSystem.terminate(), 10.seconds)
  //System.exit(0)

*/}
