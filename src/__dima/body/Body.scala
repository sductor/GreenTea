/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTeaObject.

GreenTeaObject is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GreenTeaObject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTeaObject.  If not, see <http://www.gnu.org/licenses/>.
  * */


package dima.greentea.body

  import dima._
import dima.greentea._
import dima.identifiers._
import scala.collection.mutable

////////////////////////////////////////////////////////////////
//////////////////////////// Body
////////////////////////////////////////////////////////////////


/**
 * Provide an implicit access to
 * send, order
 * knowledge
 * mailbox
 *
 **/
trait Body extends GreenTeaSeed {

  /* Communication asynchrone */

  def send(m: Performative[AgentIdentifier])

  /* Communication synchrone */

  def order[R](m: Performative[R]): R


  /*Gestion de la mail box */

  var mailbox: List[Performative]

  /*Gestion des connaissances */

  val knowledge: KnowledgeBase


  def launch

  def update
}


/* */
////////////////////////////////////////////////////////////////
//////////////////////////// Knowledge
////////////////////////////////////////////////////////////////

/**
 * Query identify a information.
 * The agent that holds the information (the referee) must be provided
 * It can be an information about internal state
 */
case class Query(referee : Identifier) extends GreenTeaObject

trait Information extends Identification[Query]

/** *
  * Knowledge build a map of information to answer a list of Queries
  * it returns true when the request are fullfill
  */
class Knowledge[Q <: Query](queries: List[Q]) extends mutable.Map[Q, Information] with Hook {

  def hook: Boolean = {
    this.size equals (queries.size)
  }
}

/**
 * A knowledge base allow agent to obtain hooks (i.e. Knowledge) about queries
 */
trait KnowledgeBase extends GreenTeaObject {

def apply (query: Query): Option[Information] = ???

def apply (queries: List[Query] ): Knowledge = ???


}

////////////////////////////////////////////////////////////////
/////////////////////////////* Asynchronicity & Self-Control */
////////////////////////////////////////////////////////////////


trait Hook extends GreenTeaObject {

  def hook: Boolean
}


object always extends Hook {
  def hook = true
}

/** ********* ***************/

protected[dima] sealed trait ActivityStatus

/** ********* ***************/

case class interrupted() extends ActivityStatus with Hook
with InitializationStatus with ExecutionStatus with TerminationStatus {

  val h: Hook

  def hook = h.hook
}

/** ********* ***************/

/*
InitializationStatus  activities
*/

protected[dima] trait InitializationStatus extends ActivityStatus

case class initialized() extends InitializationStatus

case class notInitialized() extends InitializationStatus

//implicit defs : boolean =| initialisationtype

/*
 Proactive activities
 */

protected[dima] trait ExecutionStatus extends ActivityStatus

case class continue() extends ExecutionStatus

case class stopped() extends ExecutionStatus

/*
 Proactive activities termination
 */

protected[dima] trait TerminationStatus extends ActivityStatus

case class terminated() extends TerminationStatus

case class notYet() extends TerminationStatus


