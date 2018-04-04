package com.jetprobe.core.task

import com.jetprobe.core.http.Http
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Shad.
  */
class HttpTaskSpec extends FlatSpec with Matchers{

  behavior of "HttpRequestBuilder"

  it should "parse http request params" in {
    val req = Http("login-task").get("""http://${server.name}:7070/book/100""").header("id","""${session.id}""").header("Content-type","application/json")
    val attrs : Map[String,Any] = Map("session.id" -> 1000,"server.name" -> "hostname")
    val parsedReq = HttpRequestTask.parseHttpRequest(req,attrs)
    assert(parsedReq.nonEmpty)
    parsedReq.get.uri should equal("http://hostname:7070/book/100")
    parsedReq.get.headers.size should equal(1)
    parsedReq.get.headers.get("id") should equal(Some("1000"))
  }

}
