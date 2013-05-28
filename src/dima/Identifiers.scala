/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTea.

GreenTea is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GreenTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTea.  If not, see <http://www.gnu.org/licenses/>.
  * */

package dima

import greentea._
import knowledge._

/**
 * Identifier
 */

trait Identifier extends GreenTea {

  val canonicalId : String
}


trait Identified[I <: Identifier] {

  val id: I
}


class SyncIdentifier() extends Identifier

class AsyncIdentifier() extends Identifier

/**
 * Localdentifier is the most general agent identifier
 * the one that contains the agent itself
 */

case class PrivateIdentifier[O <: GreenTea](val o: O)
                                           (implicit val key: Key, dice: Random)
  extends SyncIdentifier  {

  type Key = Long
  type Random = java.util.Random

}

/**
 * An identifier associated to a unique string
 * and generating query about the public state of it agent
 */
case class AgentIdentifier(val name: Identifier)
  extends AsyncIdentifier {

  case class QueryState(val referee: AgentIdentifier) extends Query

  case class QueryCore(val referee: AgentIdentifier) extends Query

  case class QueryAcquaintance(val referee: AgentIdentifier) extends Query

  //  case class QueryConf(val referee: AgentIdentifier) extends Query

  val state: QueryState = new QueryState(this)
  val core: QueryCore = new QueryCore(this)
  val acq : QueryAcquaintance = new QueryAcquaintance(this)
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
  * @param ref
  */
case class GroupIdentifier(groupName: Identifier, ref: AgentIdentifier)
  extends Identifier with Set[AgentIdentifier] with Query {

  var referee: AgentIdentifier = ref

  val members: Set[AgentIdentifier]

  def contains(elem: AgentIdentifier): Boolean = members contains elem

  def +(elem: AgentIdentifier): Set[AgentIdentifier] = members + elem

  def -(elem: AgentIdentifier): Set[AgentIdentifier] = members - elem

  def iterator: Iterator[AgentIdentifier] = members iterator
}

/** ***
  *
  */

class  GreenTeaComponentIdentifier extends SyncIdentifier

case class ComponentIdentifier()  extends GreenTeaComponentIdentifier

case class ProtocolIdentifier() extends GreenTeaComponentIdentifier

case class  CommandIdentifier() extends GreenTeaComponentIdentifier

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
