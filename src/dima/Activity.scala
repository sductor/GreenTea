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

import returns._
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
 * @param know
 */
class ProactivityParameters(
                             val state: State,
                             val mailbox: Option[List[Performative[PerformativeOption]]],
                             val role: Option[Role],
                             val know: Option[knowledge.Knowledge]
                             )
  extends GreenTeaObject

trait ProactivityOption extends GreenTeaOption {

  def apply(p: ProactivityParameters): Boolean
}


/**
 * Activity contains an action
 */
trait Activity[Opt <: ProactivityOption]
  extends GreenTeaCommand[Opt] with clock.Hook with Comparable[Activity[ProactivityOption]] {

  type R <: Return

  type ProactivityAction = ProactivityParameters => R

  val precedence: Int    = -1
  var isActive: Activation   = continue()

  val opt: Option[ProactivityOption]   = None
  val action: Option[ProactivityAction]   = None

  def apply(p: ProactivityParameters): R = (opt, action) match {
    case (Some(o), Some(a)) => {
      if (o(p)) {
        try {
          return a(p)
        } catch  {
         case e : Throwable => throw e//  agent.mySage.execution(new ExceptionMessage(ex, this))
        }
      } else {
           throw new RuntimeException //argh!!!
      }
    }

   case _ => throw new RuntimeException //agent.mySage.syntax(new ExceptionMessage(null,this))
  }

  /* =  {
    /
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
  with Map[ComponentIdentifier, List[Activity[ProactivityOption]]] {

  /* def get et update val state: S = agent.state*/

  def componentId: ComponentIdentifier = new ComponentIdentifier(this.getClass().toString())

  var isInitialized: Initialization = notInitialized()
  var isActive: Activation = continue()

  val activeMethods: mutable.ListMap[ComponentIdentifier, Activity[ProactivityOption]] = null
  val hookedMethods: mutable.ListMap[ComponentIdentifier, (Hook, Activity[ProactivityOption])] = null
  val stoppedMethods: mutable.ListMap[ComponentIdentifier, Activity[ProactivityOption]] = null


  /** ***
    * Introspective Functions
    */

  def updateHook() {

  }

  def execute(state: State, mailbox: List[Performative[PerformativeOption]]): Activation = continue()

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

  def terminate(): Initialization = initialized()

  /*{
  execute(proActivityTerminate())
 } */

  /**
   * Introspection
   */


  def interrupt(hook: clock.Hook) = {
    isActive = returns.interruptComponent(hook)
  }

  def stop() = {
    isActive = returns.stop()
  }

  /* */

  def interruptProactivity(proact: Activity[ProactivityOption], hook: clock.Hook)

  /* {
  assert(activeMethods contains proact)
  activeMethods -= proact
  hookedMethods +=(hook, proact)
 }  */

  def stopProactivity(proact: Activity[ProactivityOption])

  /*{
 assert(activeMethods contains proact)
 activeMethods -= proact
 stoppedMethods += proact
}     */

  /** ********************
    * PRIMTIVES
    */

  private def executeActivityMethods {
    //[P: Activity] {
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

