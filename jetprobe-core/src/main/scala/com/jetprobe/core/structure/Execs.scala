package com.jetprobe.core.structure

import java.util.function.Consumer

import com.jetprobe.core.action.builder.{ActionBuilder, PauseActionBuilder, RunnableActionBuilder}
import com.jetprobe.core.storage.Storage

import scala.concurrent.duration.FiniteDuration

/**
  * @author Shad.
  */
trait Execs[B] {

  private[core] def actionBuilders: List[ActionBuilder]

  def exec(actionBuilder: ActionBuilder): B = chain(List(actionBuilder))
  def exec(actionBuilders: ActionBuilder*): B = chain(actionBuilders.toSeq.reverse)

  def doWith[S <: Storage](config : Config[S])(handler : S => Unit) : B = chain(List(new RunnableActionBuilder[S](config,handler)))

   def chain(newActionBuilders: Seq[ActionBuilder]): B //= newInstance(newActionBuilders.toList ::: actionBuilders)
}

trait Config[S <: Storage]{

  private[jetprobe] def getStorage(sessionConf : Map[String,Any]) : S

}
