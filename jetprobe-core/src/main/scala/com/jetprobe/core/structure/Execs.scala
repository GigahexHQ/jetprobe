package com.jetprobe.core.structure

import java.util.function.Consumer

import com.jetprobe.core.task.builder.{TaskBuilder, PauseTaskBuilder, RunnableTaskBuilder}
import com.jetprobe.core.storage.Storage

import scala.concurrent.duration.FiniteDuration

/**
  * @author Shad.
  */
trait Execs[B] {

  private[core] def taskBuilders: List[TaskBuilder]

  def exec(taskBuilder: TaskBuilder): B = chain(List(taskBuilder))
  def exec(taskBuilders: TaskBuilder*): B = chain(taskBuilders.toSeq.reverse)

  def task[S <: Storage](description : String,config : Config[S])(handler : S => Unit) : B = chain(List(new RunnableTaskBuilder[S](description,config,handler)))

   def chain(newTaskBuilders: Seq[TaskBuilder]): B //= newInstance(newTaskBuilders.toList ::: taskBuilders)
}

trait Config[S <: Storage]{

  private[jetprobe] def getStorage(sessionConf : Map[String,Any]) : S

}
