package context


////////////////////////////////////////////////////////////////
//////////////////////////// Sage
////////////////////////////////////////////////////////////////


class GreenTeaException(val ex: Option[Exception], info: String = "")

implicit def exceptionToGreenTea(ex: Exception) = new GreenTeaException(Some(ex))(ex.cause())


//type ExceptionPerformative = GreenTeaObject

// type LogPerformativ = GreenTeaObject


/* Sage */

/**
 * Provide an impicit access to
 * case class syntaxError(implicit ex : Exception, info : GreenTeaString)  extends Performative[AgentIdentifier]
 * case class  communicationError(suspects : List[Performative])     extends Performative[AgentIdentifier]
 * case class  warning(implicit ex : Exception, info : GreenTeaString) extends Performative[AgentIdentifier]
 * case class  log(info : GreenTeaString) extends Performative[AgentIdentifier]
 */
trait Sage extends GreenTeaObject {

  protected[dima] case class syntaxError(gt: ExceptionPerformative)

  /*   protected[sage]
  Method executed if a protocol error is detected
   */
  protected[dima] case class communicationError(em: ExceptionPerformative)

  /*
  Method executed if an execution error is detected
   * Accessible by everyone
   */
  case class error(gt: ExceptionPerformative) extends InternalPerformative[Unit]

  /* Log */

  case class log(lm: LogPerformative) extends InternalPerformative[Unit]

}

/* Internal information required to analyse a communication fault */
class MessageTrace(val localException: Option[Exception])(implicit info: String)
  extends GreenTeaException(localException)(info) with ExceptionPerformative

trait SageTrace extends GreenTeaPerformative {

  val inreplyto: Option[List[MessageTrace]]

}


/*

 */


abstract class ExceptionOption extends dima.speech.PerformativeOption

abstract class LogOption extends PerformativeOption

abstract class ExceptionPerformative(val ex: Exception, val cause: String)
  extends PerformativeCommand[ExceptionOption]

abstract class LogPerformative extends Performative[LogOption]


//trait ExceptionPerformative extends GreenTeaException with GreenTeaPerformative[SageIdentifier]

//trait LogPerformative extends GreenTeaException with GreenTeaPerformative[SageIdentifier]
}


//
//  /** * Private identifieir
//    *
//    */
//
//  type Key = Long
//  type Random = java.util.Random
