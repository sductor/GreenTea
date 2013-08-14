/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTeaObject.

GreenTeaObject is isFreed software: you can redistribute it and/or modify
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
  package  community

  import dima.greentea._
  import java.util.Date
import scala.util.Random
  import dima.greentea.{GreenTeaCore, ExecutionStatus, GreenTeaTree}

  package object cores {

  /**
   * GreenTeaCore
   */

  /**
   * The GreenTeaCore is the shared object between the internal component of an agent
   *
   * It contains the raw values of the agent
   * And the decision function aims to be implemented in community.role
   *
   * Intents can be used to trigger component identifier activity using core introspection (see GreenTeaAgent.execute())
   *
   */
  class Core(name : String,  seed : Long = 0)
    extends GreenTeaCore(name) {

    val birthday : Date = new Date

    val random : Random = new Random(seed)

    /* Executed cyclicly before proactivity execution */
    def intentsUpdate {}

    /* Executed once at the agent initialization */
    def intentsInitiation {}

    /* return the new activity status intented of the agent */
    def intentsActivity(): ExecutionStatus = ???

    /**
     * Statee if this request of protocol exection is accepted
     * @param p the request of a protocol execution
     * @return
     */
    def accept(p: GreenTeaTree[this.type]): Boolean = true

    /* */

    def save: this.CoreSave[this.type] = ???

    def create[CoreType <: GreenTeaCore](newAgentName: String, c: GreenTeaCore#CoreSave[CoreType]): CoreType = ???

  }

  class NilCore(name : String) extends Core(name)

}