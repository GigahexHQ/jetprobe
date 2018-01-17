package com.jetprobe.core.http

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.Charset

import com.typesafe.scalalogging.StrictLogging
import io.netty.buffer.ByteBuf
import org.asynchttpclient.netty.util.ByteBufUtils

import scala.annotation.switch

/**
  * @author Shad.
  */
sealed trait ResponseBody {
  def string: String
  def bytes: Array[Byte]
  def stream: InputStream
}

object StringResponseBody extends StrictLogging {

  def apply(chunks: Seq[ByteBuf], charset: Charset) = {
    val string =
      (chunks.length: @switch) match {
        case 0 => ""
        case 1 =>
          ByteBufUtils.byteBuf2String(charset, chunks.head)
        case _ =>
          ByteBufUtils.byteBuf2String(charset, chunks: _*)
      }
    new StringResponseBody(string, charset)
  }
}

class StringResponseBody(val string: String, charset: Charset) extends ResponseBody {

  lazy val bytes = string.getBytes(charset)
  def stream = new ByteArrayInputStream(bytes)
}