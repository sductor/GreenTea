

package dima.greentea

import dima._

////////////////////////////////////////////////////////////////
/////////////////////////////* Asynchronicity & Self-Control */
////////////////////////////////////////////////////////////////


trait Hook extends GreenTeaObject {

  def isFreed: Boolean
}


object always extends Hook {
  def isFreed = true
}

/** ********* ***************/

protected[dima] sealed trait ActivityStatus

/** ********* ***************/

case class interrupted() extends ActivityStatus with Hook
with InitializationStatus with ExecutionStatus with TerminationStatus {

  var h: Option[Hook] = None

  def isFreed = h match {
    case None => true
    case Some(hook) => hook.isFreed
  }
}
            /*
def interrupted(h : Hook) : interrupted ={
  i = interrupted()
  i.h = Some(this.h)
  i
}  */

/** ********* ***************/

/*
InitializationStatus  activities
*/

sealed trait InitializationStatus extends ActivityStatus

case class initialized() extends InitializationStatus

case class notInitialized() extends InitializationStatus

//implicit defs : boolean =| initialisationtype

/*
 Proactive activities
 */

sealed trait ExecutionStatus extends ActivityStatus

case class continue() extends ExecutionStatus

case class stop() extends ExecutionStatus

/*
 Proactive activities termination
 */

sealed trait TerminationStatus extends ActivityStatus

case class terminating() extends TerminationStatus

case class terminated() extends TerminationStatus


