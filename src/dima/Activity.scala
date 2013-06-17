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
sealed abstract class Activity[+Options <: ActivityOption]
  extends GreenTeaCommand[Options] with Comparable[Activity[ActivityOption]] {

  /* State reference */

  type S <: State
  type StateStatus <: Any

  val state: S = component.getState()
  val currentStatus: Option[StateStatus] = None

  /*  Component reference */

  protected[dima] var component: ProactivityComponent[S]


  /* Activity Definition */

  type ActivityReturn <: Return
  type ActivityParameters
  type ActivityAction = ActivityParameters => ActivityReturn

  protected[dima] var options: Option[ActivityOption] = None
  protected[dima] var action: Option[ActivityAction] = None

  /* */

  //continue si ActivityReturn de type Activation et notInitialized si de type Initialisation
  protected[dima] def defaultReturn: ActivityReturn

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
   *@return  : the new activty status of this activity
   */
  def apply(mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): ActivityReturn = (options, action) match {
    case (Some(o), Some(a)) => {
      if (o(state,mailbox, knowledge)) {
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
  def getParameters(mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): ActivityParameters

  /* Comparison */

  /* statically imposed by the dima command */
  protected[dima] val systemPrecedence: Int

  /* user overridable value that will  order activities under the  same dima command */
  val precedence: Int   = 0

  def compareTo(that: Activity[ActivityOption]): Int =this.systemPrecedence.compareTo(that.systemPrecedence) match {
    case 0  => this.precedence.compareTo(that.precedence)
    case i => i
  }
}

/* Activity Commands that returns a initialization staus  (initialized / notInitialized)*/
abstract case class InitialisationActivity[Opt <: ActivityOption]() extends Activity[Opt] {

  type ActivityReturn = Initialization

  def defaultReturn = notInitialized()

}

/* Activity Commands that returns a activation status (continue / interrupt(h : Hook) / stop()) */
abstract case class ExecutionActivity[Opt <: ActivityOption]() extends Activity[Opt] {

  type ActivityReturn = Activation

  def defaultReturn = continue()

}

/* Activity Commands that returns a initialization staus  (initialized / notInitialized)*/
abstract case class TerminationActivity[Opt <: ActivityOption]() extends Activity[Opt] {

  type ActivityReturn = Termination

  def defaultReturn = terminate()

}

/* Subtype for option used for activity commands */
trait ActivityOption extends GreenTeaOption {

  /**
   * @param mailbox: List[Performative[PerformativeOption], knowledge: knowledge.KnowledgeBase : the parameters that are to give to the action
   * @return  : wether this actual parameter are valide w.r.t to the option
   */
  def apply(state : State, mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): Boolean
}


/**
 * Component :
 *
 * GreenTeaComponent is a trait that contains several activities command
 * It is implemented as a meta trait since
 *
 */
abstract class ProactivityComponent[S <: State]
  extends GreenTeaSeed with Identification[AgentIdentifier] {

  import commands.activities._
  import commands.performatives._


  /* def get et update val state: S = agent.state*/

  def componentId: ComponentIdentifier = new ComponentIdentifier(this.getClass().toString())

  type AgentState = S

  def getState(): S = agent.state

  /*
  Execution Status of this component  (handled by the agent state)
   */
  var isInitialized: Initialization = notInitialized()
  var isActive: Activation = continue()

  /*
  Activities of this component
   */

  val activeInitialisationMethods: ListBuffer[InitialisationActivity[ActivityOption]] = new ListBuffer[InitialisationActivity[ActivityOption]]
  val activeActivityMethods: ListBuffer[ExecutionActivity[ActivityOption]] = new ListBuffer[ExecutionActivity[ActivityOption]]
  val activeTerminationMethods: ListBuffer[TerminationActivity[ActivityOption]] = new ListBuffer[TerminationActivity[ActivityOption]]

  /* */

  val hookedMethods: ListBuffer[(clock.Hook, Activity[ActivityOption])] = new ListBuffer[(clock.Hook, Activity[ActivityOption])]
  val stoppedMethods: ListBuffer[Activity[ActivityOption]] = new ListBuffer[Activity[ActivityOption]]

  protected[dima] def +(act: Activity[ActivityOption]) = act match {
    case a: InitialisationActivity[ActivityOption] => activeInitialisationMethods += a
    case a: ExecutionActivity[ActivityOption] => activeActivityMethods += a
    case a: TerminationActivity[ActivityOption] => activeTerminationMethods += a
  }


  protected[dima] def -(act: Activity[ActivityOption]) = act match {
    case a: InitialisationActivity[ActivityOption] => activeInitialisationMethods -= a; stoppedMethods += a
    case a: ExecutionActivity[ActivityOption] => activeActivityMethods -= a; stoppedMethods += a
    case a: TerminationActivity[ActivityOption] => activeTerminationMethods -= a; stoppedMethods += a
  }


  protected[dima] def ~(act: Activity[ActivityOption], h: interrupted) = {
    val m = (h.hook, act)
    act match {
      case a: InitialisationActivity[ActivityOption] => activeInitialisationMethods -= a; hookedMethods += m
      case a: ExecutionActivity[ActivityOption] => activeActivityMethods -= a; hookedMethods += m
      case a: TerminationActivity[ActivityOption] => activeTerminationMethods -= a; hookedMethods += m
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

  def interruptActivity(proact: Activity[ActivityOption], hook: clock.Hook) =
    this ~(proact, returns.interrupt(hook))

  def stopActivity(proact: Activity[ActivityOption]) = this - proact

  def reactivateActivity(proact: Activity[ActivityOption]) = this + proact

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