package com.jetprobe.core.runner

import java.util.Date

import akka.actor.{ActorRef, PoisonPill, Props}
import com.jetprobe.core.Predef.Session
import com.jetprobe.core.action.{Action, BaseActor, Exit}
import com.jetprobe.core.controller.ControllerCommand.EndScenario
import com.jetprobe.core.runner.ScenarioManager._
import com.jetprobe.core.session.Session
import com.jetprobe.core.structure.{ExecutableScenario, Scenario}
import com.jetprobe.core.validations.{Failed, Passed, Skipped}

import scala.concurrent.duration.FiniteDuration

/**
  * @author Shad.
  */
class ScenarioManager(runnableScn : ExecutableScenario, controller : ActorRef) extends BaseActor {

  var startTime :Long = 0L
  var session: Session = _
  override def receive: Receive = {

    case StartScenario =>
      startTime = new Date().getTime
      val scn = runnableScn.build(context.system,new Exit(self),self)
      session = Session(scn.name, scn.className, attributes = scn.configAttr)
      scn.entry.execute(session)

    case ActionCompleted(newSession) =>
      session = newSession
      context stop(sender())

    case ExecuteNext(action,newSession,isScheduled) =>
      if(!isScheduled){
        context stop(sender())
      }
      logger.info(s"Executing next action ${action.name}")
      action.execute(newSession)

    case ScenarioCompleted(finalSession) =>
      val status = if (finalSession.validationResuls.count(_.status == Passed) == finalSession.validationResuls.size) {
        Passed
      } else if (finalSession.validationResuls.filter(_.status == Skipped).size > 0) {
        Skipped
      } else Failed

      val timeTaken = (new Date().getTime - startTime)/1000f
      controller ! EndScenario(finalSession,timeTaken,status)


    case ExecuteWithDelay(next,delay) =>
      logger.info(s"Pausing the pipeline for the duration : ${delay.length}")
      context.system.scheduler.scheduleOnce(delay,self,ExecuteNext(next,session,true))
  }
}

object ScenarioManager {

  case class StartScenario()

  case class ActionCompleted(session: Session)

  case class ExecuteNext(next : Action,session: Session,scheduledAction : Boolean)

  case class ExecuteWithDelay(next : Action,duration: FiniteDuration)

  case class ScenarioCompleted(session: Session)


  def props(scn : ExecutableScenario, controller : ActorRef) : Props = Props(new ScenarioManager(scn,controller))

}
