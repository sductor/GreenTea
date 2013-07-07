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

package community.context
import dima._
import dima.identifiers._
import greentea._

import scala.collection.parallel.mutable


package object context {


  type Body = GreenTeaSeed {

    /* Démarrage du thread */

    def launch

    /* Communication synchrone */

    def order[R](m: Performative[R]): R

  }


  /**
 * Communication component should be implemented as objects and imported
 * They will then transparently handle the  communication that has this I identifier type
 * @tparam I  : the Identifier associated to a communication canal that this object is adapted.
 */
trait CommunicationComponent[I <: Identifier] extends GreenTeaObject {

  type P <: Performative[I]

  def send(m: P)

  def flushMail: List[P]
}

/**
 * Sources provides transparently asynchronous Knowledge
 * They are associated to a specific case tree of Query : Q
 * They should define this case tree and implement apply for every case of this tree
 * e.g.
 * @tparam Q : The key parsed by the map
 */

trait Source[Q <: Query] extends Proactivity with mutable.Map[Q, Information] with Identification[AgentIdentifier] {

  /* Return a knowledge */
  def apply(queries: List[Q]): Knowledge[Q]

  /* register the agents for the query */
  def apply(observer: Identifier, queries: List[Query], hook: Hook = always)
}

/* e.g : The request is used as the key of Source[SQL] */
//case class SQL(request: String) extends Query

}