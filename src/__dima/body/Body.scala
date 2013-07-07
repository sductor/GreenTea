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

import _root_.dima._
import community.{nativecommands, nativeoptions}
import dima.greentea._
import dima.speech._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

package context

import greentea._

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

  def order[R](m: SyncPerformative[R]): R


  /*Gestion de la mail box */

  var mailbox: List[Performative]

  /*Gestion des connaissances */

  val knowledge: KnowledgeBase


  def launch

  def update
}

////////////////////////////////////////////////////////////////
//////////////////////////// Communication
////////////////////////////////////////////////////////////////


class Identifier(val name: String) extends GreenTeaObject

implicit def stringToIdentifier (name: String): Identifier = new Identifier (name)

type Performative[I <: Identifier] = GreenTeaObject

type InternalPerformative[Return] = GreenTeaObject

/* */


/* */
////////////////////////////////////////////////////////////////
//////////////////////////// Knowledge
////////////////////////////////////////////////////////////////

/**
 * Query identify a information.
 * The agent that holds the information (the referee) must be provided
 * It can be an information about internal state
 */
case class Query(referee: AgentIdentifier) extends Identifier(referee.name) {

  type I <: Information

}

type Information = Identification[Query]

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
type KnowledgeBase = GreenTeaObject {

import scala.collection.mutable

def apply (query: Query): Option[Information]

def apply (queries: List[Query] ): Knowledge


}

////////////////////////////////////////////////////////////////
/////////////////////////////* Asynchronicity & Self-Control */
////////////////////////////////////////////////////////////////


trait Hook extends GreenTeaObject {

  def hook: Boolean = apply
}


object always extends Hook {
  def hook = true
}

/** ********* ***************/

protected[dima] sealed trait ActivityHook extends Hook

/** ********* ***************/

case class interrupted() extends ActivityHook with Hook
with InitializationHook with ExecutionHook with TerminationHook {

  val h: Hook

}

def interrupt (h: Hook) = {
val i = new interrupted ()

}

/** ********* ***************/

/*
InitializationHook  activities
*/

protected[dima] trait InitializationHook extends ActivityHook

case class initialized() extends InitializationHook

case class notInitialized() extends InitializationHook

//implicit defs : boolean =| initialisationtype

/*
 Proactive activities
 */

protected[dima] trait ExecutionHook extends ActivityHook

case class continue() extends ExecutionHook

case class stopped() extends ExecutionHook

/*
 Proactive activities termination
 */

protected[dima] trait TerminationHook extends ActivityHook

case class terminated() extends TerminationHook

case class notYet() extends TerminationHook


