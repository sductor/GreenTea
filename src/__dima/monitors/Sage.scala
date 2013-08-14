package dima.monitors

import dima.dsl.PerformativeOption
import dima.greentea._
import dima.dsl._
import dima._

////////////////////////////////////////////////////////////////
//////////////////////////// Sage
////////////////////////////////////////////////////////////////


class GreenTeaException(exTrace: Option[Throwable] = None, messTrace: Option[MessageTrace] = None, info: String = "")
  extends GreenTeaPerformative[AsynchronousExecution,ExceptionOptions]

abstract class ExceptionOption extends PerformativeOption

/* Internal information required to analyse a communication fault */
trait MessageTrace {

  val localException: Option[Throwable] = None

  val info: String = ""

  //val inreplyto: Option[List[MessageTrace]]

}


//type ExceptionPerformative = GreenTeaObject

// type LogPerformativ = GreenTeaObject


/* Sage */

/**
 * Provide an impicit access to
 * case class syntaxError(implicit ex : Exception, info : GreenTeaString)  extends Message[AgentIdentifier]
 * case class  communicationError(suspects : List[Message])     extends Message[AgentIdentifier]
 * case class  warning(implicit ex : Exception, info : GreenTeaString) extends Message[AgentIdentifier]
 * case class  log(info : GreenTeaString) extends Message[AgentIdentifier]
 */
class Sage {//extends GreenTeaLeaf {

  //val monitoredAgent : GreenTeaAgent[_]

  def execute = ???

  //fournit des reactionFor les exceptions performative

  protected[dima] def syntaxError(gt: Option[GreenTeaException] = None) = ???

  /*   protected[monitors]
  Method executed if a protocol error is detected
   */
  protected[dima] def communicationError(em: Option[GreenTeaException] = None)  = ???

  /*
  Method executed if an execution error is detected
   * Accessible by everyone
   */
  def error(gt: Option[GreenTeaException] = None) = ???

  /*
  /* Log */

  case c log(lm: LogPerformative) extends Message[Any]
     */
}


/*

*/


//trait ExceptionPerformative extends GreenTeaException with GreenTeaPerformative[SageIdentifier]

//trait LogPerformative extends GreenTeaException with GreenTeaPerformative[SageIdentifier]
//}


//
//  /** * Private identifieir
//    *
//    */
//
//  type Key = Long
//  type Random = java.util.Random
