package com.jetprobe.core.session

import java.util.Date

import com.jetprobe.core.generator.Generator
import com.jetprobe.core.validations.ValidationResult
import com.typesafe.scalalogging.LazyLogging

/**
  * @author Shad.
  */
case class Session(
                    testName: String,
                    className : String,
                    attributes: Map[String, Any] = Map.empty,
                    records: Generator = Iterator.empty,
                    validationResuls : Seq[ValidationResult] = Seq(),
                    startDate: Long = new Date().getTime,
                    onExit: Session => Unit = Session.NothingOnExit
                  ) extends LazyLogging

object Session {

  val Identity: Session => Session = identity[Session]
  val NothingOnExit: Session => Unit = _ => ()
}

case class UserMessage(
                        session: Session,
                        timestamp: Long
                      )