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

package dima.activity

import dima._
import dima.returns._
import dima.speech._
import scala.collection.mutable._
import dima.knowledge.KnowledgeBase
import scala._
import dima.returns.notInitialized
import dima.returns.stop
import dima.returns.notYet
import dima.returns.initialized
import dima.returns.interrupted
import dima.returns.terminate
import dima.returns.continue
import dima.returns.notInitialized
import dima.returns.stop
import dima.returns.notYet
import dima.returns.initialized
import dima.returns.interrupted
import dima.returns.terminate
import dima.ComponentIdentifier
import dima.CommandIdentifier
import dima.returns.continue
import dima.returns.notInitialized
import dima.returns.stop
import scala.Some
import dima.returns.notYet
import dima.returns.initialized
import dima.returns.interrupted
import dima.returns.terminate
import dima.ComponentIdentifier
import dima.CommandIdentifier
import dima.returns.continue
import dima.returns.notInitialized
import dima.returns.stop
import scala.Some
import dima.returns.notYet
import dima.returns.initialized
import dima.returns.interrupted
import dima.returns.terminate
import dima.ComponentIdentifier
import dima.CommandIdentifier
import dima.returns.continue

/**
 *
 * This files contains the basic components for activity description and execution
 */

/**
 * Activity is the metaclass for activity commands.
 * It contains an action that is the function executed by the activity
 * This action is a function of type ActivityAction whose parameters and return type are defined by the options.
 * precedence is used by the comparator to sort the different activities
 */
abstract class Activity[+R <: Return](protected[dima] val agent: GreenTeaAgent,
                                      protected[dima] val component: ProactivityComponent)
  extends GreenTeaCommand with Comparable[Activity[Return]] {

  val id: CommandIdentifier = component.newCommandId

  /* State reference */

  type StateStatus
  val currentStatus: Option[StateStatus] = None


  /* Activity Definition */

  type ActivityReturn <: Return
  type ActivityParameters
  type ActivityAction = ActivityParameters => ActivityReturn
  type OptionObject <: ActivityOption


  protected[dima] var action: Option[ActivityAction] = None


  /* Return */

  implicit var r: R = _

  //continue si ActivityReturn de type Activation et notInitialized si de type Initialisation
  protected[dima] def defaultReturn: ActivityReturn = r match {
    case a: Initialization => notInitialized()
    case a: Execution => continue()
    case a: Termination => terminate()
  }


  implicit def defaultReturn(a: Any): ActivityReturn = defaultReturn

  /* */

  /**
   * Used for  activity description
   * Defines the action and register the activity to the component
   * @param a : the associated action
   */
  def is(a: ActivityAction) = {
    action = Some(a)
    component + this
  }

  /**
   * Used to execute the method
   * @param mailbox: List[Performative[PerformativeOption]]
   * @param  knowledge            knowledge: knowledge.KnowledgeBase : the agent current external state
   * @return  : the new activty status of this activity
   */
  def apply(mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): ActivityReturn = (options, action) match {

    case (Some(o), Some(a)) => {
      val p = new ActivityOptionParameters(state(), mailbox, knowledge)
      if (o(p)) {
        try {
          return a(getParameters(mailbox, knowledge))
        } catch {
          case e: Throwable => throw e //  agent.mySage.execution(new ExceptionPerformative(ex, this))
        }
      } else {
        defaultReturn
      }
    }

    case _ => throw new RuntimeException //agent.mySage.syntax(new ExceptionPerformative(null,this))
  }

  /**
   * Convert the mailbox and the knowledge into this specific activity parameter and execute "execute" function
   * @param mailbox
   */
  def getParameters(mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): this.ActivityParameters


  /* Comparison */

  /* statically imposed by the dima command */
  protected[dima] val systemPrecedence: Int

  /* user overridable value that will  order activities under the  same dima command */
  val precedence: Int = 0

  def compareTo[R <: Return](that: Activity[R]): Int = this.systemPrecedence.compareTo(that.systemPrecedence) match {
    case 0 => this.precedence.compareTo(that.precedence)
    case i => i
  }

}

class ActivityOptionParameters(s: State, mails: List[Performative[PerformativeOption]], k: dima.knowledge.KnowledgeBase)

/* Subtype for option used for activity commands */
class ActivityOption extends GreenTeaOption {

  /**
   * @param mailbox: List[Performative[PerformativeOption], knowledge: knowledge.KnowledgeBase : the parameters that are to give to the action
   * @return  : wether this actual parameter are valide w.r.t to the option
   */
  type OptionParameters = ActivityOptionParameters


  /**
   * State wether the action is executable with the provided option parameters
   * @param op
   * @return
   */
  def apply(op: ActivityOptionParameters): Boolean = ???
}


/**
 * Component :
 *
 * GreenTeaComponent is a trait that contains several activities command
 * It is implemented as a meta trait since
 *
 */
class ProactivityComponent(implicit protected[dima] val agent: GreenTeaAgent)
  extends GreenTeaSeed with Identification[AgentIdentifier] {

  import dima.commands.activities._
  import dima.commands.performatives._


  val id: AgentIdentifier = agent.id

  /* def get et update val state: S = agent.state*/

  def componentId: ComponentIdentifier = new ComponentIdentifier(this.getClass().toString())

  var commandId: Int = 0

  def newCommandId = new CommandIdentifier(componentId + "#CommandNumber#" + commandId);
  commandId += 1;

  /*
  Execution Status of this component  (handled by the agent state)
   */
  var isInitialized: Initialization = notInitialized()
  var isActive: Execution = continue()

  /*
  Activities of this component
   */

  val activeInitialisationMethods: ListBuffer[Activity[Initialization]] = new ListBuffer[Activity[Initialization]]
  val activeActivityMethods: ListBuffer[Activity[Execution]] = new ListBuffer[Activity[Execution]]
  val activeTerminationMethods: ListBuffer[Activity[Termination]] = new ListBuffer[Activity[Termination]]

  /* */

  val hookedMethods: ListBuffer[(clock.Hook, Activity[Return])] = new ListBuffer[(clock.Hook, Activity[Return])]
  val stoppedMethods: ListBuffer[Activity[Return]] = new ListBuffer[Activity[Return]]

  protected[dima] def +[R <: Return](act: Activity[R]) = act match {
    case a: Activity[Initialization] => activeInitialisationMethods += a
    case a: Activity[Execution] => activeActivityMethods += a
    case a: Activity[Termination] => activeTerminationMethods += a
  }


  protected[dima] def -[R <: Return](act: Activity[R]) = act match {
    case a: Activity[Initialization] => activeInitialisationMethods -= a; stoppedMethods += a
    case a: Activity[Execution] => activeActivityMethods -= a; stoppedMethods += a
    case a: Activity[Termination] => activeTerminationMethods -= a; stoppedMethods += a
  }


  protected[dima] def ~[R <: Return](act: Activity[R], h: interrupted) = {
    val m = (h.hook, act)
    act match {
      case a: Activity[Initialization] => activeInitialisationMethods -= a; hookedMethods += m
      case a: Activity[Execution] => activeActivityMethods -= a; hookedMethods += m
      case a: Activity[Termination] => activeTerminationMethods -= a; hookedMethods += m
    }
  }

  /** ***
    * Execution Functions
    */


  def execute(mailbox: List[Performative[PerformativeOption]], knowledge: KnowledgeBase) = isActive match {
    case h: interrupted =>
      if (h.hook.isReactivated) {
        isActive = continue()
      }

    case _: continue => {
      def updateActivityHooks() {
        hookedMethods.foreach {
          m =>
            val (hook, act) = m
            if (hook.isReactivated()) {
              hookedMethods -= m
              this + (act)
            }
        }
      }
      updateActivityHooks()
      isInitialized match {

        case notInitialized() =>
          isInitialized = initialized() //default initialization if all method are ok
          activeInitialisationMethods.foreach {
            m => m(mailbox, knowledge) match {
              case _: notInitialized => isInitialized = notInitialized()
              case i: interrupted => this ~(m, i)
              case _: initialized => this - m
            }
          }

        case initialized() =>
          activeActivityMethods.foreach {
            m => m(mailbox, knowledge) match {
              case _: continue => /* Ze good */
              case i: interrupted => this ~(m, i)
              case _: stop => this - m
            }
          }
      }
    }


    case _: stop =>
      activeTerminationMethods.foreach {
        m => m(mailbox, knowledge) match {
          case _: terminate => this - m
          case i: interrupted => this ~(m, i)
          case _: notYet => /* do not move */
        }
      }
  }

  /**
   * Introspection :
   * allows an external control  of the component behavior
   */

  /*
  Those methods allow to globally control the set of activity functions
   */

  def interruptComponent(hook: clock.Hook) = {
    isActive = returns.interrupt(hook)
  }

  def stopComponent() = {
    isActive = returns.stop()
  }

  def reactivateComponent() = {
    isActive = returns.continue()
  }

  /*
  Those methods allow to indivually control each activity
  */

  def interruptActivity[R <: Return](proact: Activity[R], hook: clock.Hook) =
    this ~(proact, returns.interrupt(hook))

  def stopActivity[R <: Return](proact: Activity[R]) = this - proact

  def reactivateActivity[R <: Return](proact: Activity[R]) = this + proact

}


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
    interrupt (h) => {
    stepMethods.get (preActivity () ) -= m
    assert ! interruptMethods.get (preActivity () ).contains (m)
    interruptMethods.get (preActivity () ) += m
    hook += (h, m)
    }
    stop (h) => stepMethods.get (preActivity () ) -= m
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