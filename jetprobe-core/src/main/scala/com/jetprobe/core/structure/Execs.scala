package com.jetprobe.core.structure

import com.jetprobe.core.action.builder.ActionBuilder

/**
  * @author Shad.
  */
trait Execs[B] {

  private[core] def actionBuilders: List[ActionBuilder]
  private[core] def newInstance(actionBuilders: List[ActionBuilder]): B

  def exec(actionBuilder: ActionBuilder): B = chain(List(actionBuilder))
  def exec(actionBuilders: ActionBuilder*): B = chain(actionBuilders.toSeq.reverse)

  private[core] def chain(newActionBuilders: Seq[ActionBuilder]): B = newInstance(newActionBuilders.toList ::: actionBuilders)
}
