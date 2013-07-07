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

import dima.greentea._
import dima.speech._

import scala.collection.mutable
import community.ActivitiesOption.ActivityOption
import sun.management.Agent

/**
 *
 * This files contains the basic components for activity description and execution
 */


////////////////////////////////////////////////////////////////
//////////////////////////// ProaActivity  Implementation
////////////////////////////////////////////////////////////////


/**
 * Activity is the metaclass for activity commands.
 * It contains an action that is the function executed by the activity
 * This action is a function of type ActivityAction whose parameters and return type are defined by the options.
 * precedence is used by the comparator to sort the different activities
 *
 * Ordre d'execution :
 *
 * Initialisation loop :
 *    *
 *
 * les preactivity
 * Les reactif single par mailbox
 * Les reactif multiple
 *
 *
 */
protected[dima] class Proactivity[+Return <: ActivityHook](implicit val component: ComponentIdentifier)
  extends GreenTeaLeaf with Comparable[Proactivity[ActivityHook]] {

  val name: String

  /* Activity Definition */

  type ActivityParameters = Unit

  type ActivityAction = ActivityParameters => Return


  var action: Option[ActivityAction] = None

  type Options = List[GreenTeaOption] with Hook

  var options: Option[ActivityOption] = None

  /* Return */

  implicit var r: Return = _

  //continue si ActivityReturn de type Activation et notInitialized si de type Initialisation
  implicit def defaultReturn: Return = r match {
    case a: InitializationHook => notInitialized()
    case a: ExecutionHook => continue()
    case a: TerminationHook => terminated()
  }


  implicit def defaultReturn(a: Any): Return = defaultReturn

  /* */


  /**
   * Used to execute the method
   * @return  : the new activty status of this activity
   */
  def apply(): Return = (options, action) match {

    case (Some(o), Some(a)) => {
      if (o.hook) {
        try {
          return a(o.getParameters())
        } catch {
          case e: Throwable => throw e //  agent.mySage.execution(new ExceptionPerformative(ex, this))
        }
      } else {
        defaultReturn
      }
    }

    case _ => throw new RuntimeException //agent.mySage.syntax(new ExceptionPerformative(null,this))
  }
}
 object proactivity extends Proactivity

protected[dima] class Reactivity[+Return <: ActivityHook, P <: Performative](val key: P) extends Proactivity[Return](1) {


  type ActivityAction = P  =>  ActivityParameters => ExecutionHook

  /**
   * Used to execute the method
   * @return  : the new activty status of this activity
   */
  def apply(m: P): ExecutionHook = ???
}

protected[dima] class Preactivity[Return <: ActivityHook] extends Proactivity[Return](0)



/*{
assert(activeMethods contains proact)
activeMethods -= proact
stoppedMethods += proact
}     */

/** ********************
  * PRIMTIVES
  */

//private def executeActivityMethods {
//[P: Activity] {
/* stepMethods.filter(t).foreach({
  m =>
    m() match {
    interrupted (h) => {
    stepMethods.get (preActivity () ) -= m
    assert ! interruptMethods.get (preActivity () ).contains (m)
    interruptMethods.get (preActivity () ) += m
    hook += (h, m)
    }
    stopped (h) => stepMethods.get (preActivity () ) -= m
    }
}  */


/**
 * Activity methods' action have optional parameters.
 * This class is used to stock them when constructing the action
 * The parameters are curryfied to be passed to the action
 *
 * @param messages the events received for a reactivity method
 * @param knowing the result of asynchronous call

protected[dima] class ProactivityParameters(
                                             val messages: Option[List[Performative[PerformativeOption]]]
val know: Option[knowledge.Knowledge]
)
extends GreenTeaObject
 */


/*
/*
Those methods allow to indivually control each activity
*/

def interruptActivity[R <: Return](proact: Activity[R], hook: clock.Hook) =
  this ~(proact, returns.interrupted(hook))

def stopActivity[R <: Return](proact: Activity[R]) = this - proact

def reactivateActivity[R <: Return](proact: Activity[R]) = this + proact
      */