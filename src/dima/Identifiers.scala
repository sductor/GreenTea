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

package dima

import dima._
import knowledge._

/**
 * Identifier
 */
// extends GreenTeaObject {

//val canonicalId: String
//}


trait Identification[I <: Identifier] {

  val id: I
}


abstract class SyncIdentifier() extends Identifier

class GreenTeaComponentIdentifier extends SyncIdentifier

case class ComponentIdentifier(id: String) extends GreenTeaComponentIdentifier

case class ProtocolIdentifier() extends GreenTeaComponentIdentifier

case class CommandIdentifier() extends GreenTeaComponentIdentifier

/**
 * Localdentifier is the most general agent identifier
 * the one that contains the agent itself
 */

case class PrivateIdentifier[O <: GreenTeaObject with Identification[Identifier]](val o: O)
                                                                                 (implicit val key: Key, dice: Random)
  extends SyncIdentifier {

  //val canonicalId: String = o.id.canonicalId
}





abstract class AsyncIdentifier() extends Identifier


/**
 * An identifier associated to a unique string
 * and generating query about the public state of it agent
 */
class AgentIdentifier(val name: Identifier)
  extends AsyncIdentifier {

  // val canonicalId: String = name.canonicalId

  case class QueryState(val referee: AgentIdentifier) extends Query

  case class QueryCore(val referee: AgentIdentifier) extends Query

  case class QueryAcquaintance(val referee: AgentIdentifier) extends Query

  //  case class QueryConf(val referee: AgentIdentifier) extends Query

  val state: QueryState = new QueryState(this)
  val core: QueryCore = new QueryCore(this)
  val acq: QueryAcquaintance = new QueryAcquaintance(this)
  //retourne la liste des groupes
}


// implicit def privateToAgent(l: PrivateIdentifier): AgentIdentifier =
//   new AgentIdentifier(l.agent.id)

/*

 */

/** **
  * GroupIdentifier allow to :
  * Characterize interacting agents
  * Allow artefact handling
  *
  * @param groupName
  * @param
  */
class GroupIdentifier(val groupName: Identifier, var groupReferee: AgentIdentifier)
  extends AgentIdentifier(groupName) with Set[AgentIdentifier] with Query {

  val referee: AgentIdentifier = this

  var members: Set[AgentIdentifier] = _

  def contains(elem: AgentIdentifier): Boolean = members contains elem

  def +(elem: AgentIdentifier): Set[AgentIdentifier] = members + elem

  def -(elem: AgentIdentifier): Set[AgentIdentifier] = members - elem

  def iterator: Iterator[AgentIdentifier] = members iterator
}

/** ***
  *
  */


/* */

class ArtefactIdentifier() extends AsyncIdentifier

/* */

case class MessageIdentifier() extends ArtefactIdentifier

case class ConversationIdentifier() extends ArtefactIdentifier

/* */

case class ClockIdentifier() extends ArtefactIdentifier

case class StateIdentifier() extends ArtefactIdentifier

case class KnowledgeSourceIdentifier() extends ArtefactIdentifier

case class SourceIdentifier() extends ArtefactIdentifier
