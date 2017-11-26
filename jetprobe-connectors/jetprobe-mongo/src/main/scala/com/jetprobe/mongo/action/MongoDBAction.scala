package com.jetprobe.mongo.action

import com.jetprobe.core.action.Action
import com.jetprobe.core.session.Session

/**
  * @author Shad.
  */
class MongoDBAction extends Action{

  override def name: String = ???

  override def execute(session: Session): Unit = ???


}

sealed trait MongoIOActionDef


case class CreateCollection(database : String, collection : String)
case class InsertRows(database : String,collection : String, rows : Iterator[String])

