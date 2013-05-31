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

import commands.returns._
import dima._


import clock.Hook
import scala.collection.mutable
import dima.sage.ExceptionMessage

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
 * Activity contains an action
 * @param opt
 * @tparam R
 * @tparam Opt
 */
trait Activity[Opt <: ProactivityOption]
  extends GreenTeaCommand[Opt] with clock.Hook with Comparable[Activity[ProactivityOption]] {

  type R <: Return

  type ProactivityAction  = ProactivityParameters => R

  val precedence: Int
  = -1
  var isActive: Activation
  = continue()

  val opt: Option[ProactivityOption]
  = None
  val action: Option[ProactivityAction]
  = None


  def apply(p: ProactivityParameters): R
/* =  {
  /if (opt(p)) {
     try {
     action(p)
     } catch {
       agent.mySage.execution(new ExceptionMessage(p))
     }
   }
 }*/
}

/**
* Component :
*
*
*
*/
trait GreenTeaComponent[S <: State]
 extends GreenTeaSeed with Identification[AgentIdentifier]
 with Activity[Activation]
 with Set[Activity[Return]]
 with Map[ComponentIdentifier, List[Activity[ProactivityOption]]] {

 protected[dima] implicit val agent: Agent[S]

 val state: S = agent.state

 def componentId: ComponentIdentifier = new ComponentIdentifier(this.getClass().toString())

 var isInitialized: Initialization = notInitialized()

 val activeMethods: mutable.ListMap[ComponentIdentifier, Activity[ProactivityOption]] = null
 val hookedMethods: mutable.ListMap[ComponentIdentifier, (Hook, Activity[ProactivityOption])] = null
 val stoppedMethods: mutable.ListMap[ComponentIdentifier, Activity[ProactivityOption]] = null


 /** ***
   * Introspective Functions
   */

 def updateHook() {

 }

 def execute(state: State, mailbox: List[Message]): Activation = continue()
/* = {
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

}*/

def terminate():Initialization = initialized() /*{
 execute(proActivityTerminate())
} */

/**
* Introspection
*/


def interrupt(hook: clock.Hook) = {isActive = dima.commands.returns.interruptComponent(hook)}

def stop() = {isActive = dima.commands.returns.stop()}

/* */

def interruptProactivity(proact: Activity[ProactivityOption], hook: clock.Hook) /* {
 assert(activeMethods contains proact)
 activeMethods -= proact
 hookedMethods +=(hook, proact)
}  */

def stopProactivity(proact: Activity[ProactivityOption])  /*{
 assert(activeMethods contains proact)
 activeMethods -= proact
 stoppedMethods += proact
}     */

/** ********************
 * PRIMTIVES
 */

private def executeActivityMethods[P : Activity] {
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

