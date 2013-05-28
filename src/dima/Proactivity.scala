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

import _root_.commands.returns.{continue, Activation, Return}
import greentea._


import clock.Hook
import scala.collection.mutable

/**
 * The universal arguments,
 * arguments are extract from option
 * and passed in always this order, None are ommited
 *
 * @param state
 * @param mailbox
 * @param role
 * @param Knowledge
 */
class ProactivityParameters(
                             val state: State,
                             val mailbox: Option[List[Message]],
                             val role: Option[Role],
                             val Knowledge: Option[knowledge.Knowledge]
                             )
  extends GreenTea

trait ProactivityOption extends GreenTeaOption {

  def apply(p: ProactivityParameters): Boolean
}
object ProactivityOption

/**
 * Proactivity contains an action
 * @param opt
 * @tparam R
 * @tparam Opt
 */
class Proactivity[Opt <: ProactivityOption]
(implicit opt: ProactivityOption)
  extends GreenTeaCommand[Opt] with clock.Hook with Comparable[Proactivity[ProactivityOption]] {

  type R <: Return
  type ProactivityAction = ProactivityParameters => R

  val precedence: Int = -1
  val isActive: Activation = continue()

  var opt: Option[ProactivityOption] = None
  var action: Option[ProactivityAction] = None


  def apply(p: ProactivityParameters): R = {
    if (opt(p)) {
      try {
      action(p)
      } catch {
        agent.mySage.execution(new ExceptionMessage(p))
      }
    }
  }
}

/**
 * Component :
 *
 *
 *
 */
trait GreenTeaComponent
  extends GreenTeaSeed[AgentIdentifier] with Proactivity[Activation]
  with List[Proactivity[Return]]
  with Map[ComponentIdentifier, List[Proactivity[Return]]] {

  protected[dima] implicit val agent: Agent

  val state: agent.MyStateType = agent.state

  def componentId: ComponentIdentifier = new ComponentIdentifier(this.getClass())

  var isInitialized: InitializationType = notIinitialized()
  var isActive: ActivationType = continue()

  val activeMethods: mutable.ListMap[ComponentIdentifier, Proactivity] = null
  val hookedMethods: mutable.ListMap[ComponentIdentifier, (Hook, Proactivity)] = null
  val stoppedMethods: mutable.ListMap[ComponentIdentifier, Proactivity] = null


  /** ***
    * Introspective Functions
    */

  def updateHook() {

  }

  def execute(state: State, mailbox: List[Message]): ActivationType = {
    /*
    //Routine d'initialisation :
    //tente l'initialisation par comosant
    def initialize(): InitializationType {
      isInitialized = notInitialized
      stepMethods.get (proActivityInitialise () ).foreach (m =>
      myProactivityIsInitialized = match (m.execute () ) {

    }
      )
      myProactivityIsInitialized
    }




    //initialisation
    if (! myProactivityIsInitialized) {
    minitialize
  }

    //execution loop
    else if (state.imAlive) {

    updateHook

    executeMethods (preactivity () )
    executeMessageMethod ()
    execute (activity () )
    execute (postActivity () )

  }
  */
  }

  def terminate() {
    execute(proActivityTerminate())
  }

  /**
   * Introspection
   */


  def interrupt(hook: clock.Hook) {
    isActive = interrupt(hook)
  }

  def stop() {
    isActive = stop()
  }

  /* */

  def interruptProactivity(proact: Proactivity, hook: clock.Hook) {
    assert(activeMethods contains proact)
    activeMethods -= proact
    hookedMethods +=(hook, proact)
  }

  def stopProactivity(proact: Proactivity) {
    assert(activeMethods contains proact)
    activeMethods -= proact
    stoppedMethods += proact
  }

  /** ********************
    * PRIMTIVES
    */

  private def executeActivityMethods[P : Proactivity] {
    /* stepMethods.filter(t).foreach({
      m =>
        m() match {
        interrupt (h) => {
        stepMethods.get (preActivity () ) -= m
        assert ! interruptMethods.get (preActivity () ).contains (m)
        interruptMethods.get (preActivity () ) += m
        hook += (h, m)
        }
        stop (h) => stepMethods.get (preActivity () ) -= m
        }
    }  */
  }
}

