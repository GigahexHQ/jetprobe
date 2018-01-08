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
  //private[core] def newInstance(actionBuilders: List[ActionBuilder]): B

  def exec(actionBuilder: ActionBuilder): B = chain(List(actionBuilder))
  def exec(actionBuilders: ActionBuilder*): B = chain(actionBuilders.toSeq.reverse)

  /**
    * An Action constructor for building action against the target system or storage.
    *
    * @param storage The Storage instance, against which the actions would be executed
    * @param handler The set of actions to be executed
    * @tparam T
    * @return
    */
  def doWith[T <: Storage](storage : T)(handler : T => Unit) : B = chain(List(new RunnableActionBuilder[T](storage,handler)))

  def doWith[S <: Storage](config : Config[S])(handler : S => Unit) : B = chain(List(new RunnableActionBuilder[S](config.getStorage,handler)))


   def chain(newActionBuilders: Seq[ActionBuilder]): B //= newInstance(newActionBuilders.toList ::: actionBuilders)
}

trait Config[S <: Storage]{

  private[jetprobe] def getStorage : S

}
