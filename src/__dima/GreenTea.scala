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

package dima.greentea

import dima._
import dima.identifiers._
import body._
import dima.sage._
import scala.collection.mutable
import scala.concurrent.Promise

//////////////////////////////////    //////////////////////////////////
// //////////////////////// GREENTEA IMPLEMENTATION
// ///////////////////////////////   //////////////////////////////////

/**
 * Those are the atomic components that have different access to the agent
 */


trait GreenTeaSeed extends GreenTeaObject {

  protected[dima] implicit val id: AgentIdentifier


  protected[dima] implicit val body: Body

  protected[dima] implicit val sage: Sage

}

/**
 * GreenTeaLeaf :
 *
 * GreenTeaComponent is a trait that contains several activities command
 * It is implemented as a meta trait since
 *
 */

trait GreenTeaLeaf[S <: Core] extends GreenTeaSeed
with Identification[ComponentIdentifier] {

  protected[dima] implicit val agentSpecification: Manifest[Agent[S]]

  /**
   * Provide an access to  the agent state
   *
   */
  protected[dima] var state: S


  /* Proactivity Implementation */


  val id: ComponentIdentifier = ???

  class ActivityStatus {


  }


  /* The lists of auto-plugged GreenTeaCommand
   *  They are map by type and the list is sorted according to the user precedence def
    *
    * */

  val proactivities: mutable.Map[Proactivity[ActivityHook], List[Proactivity[ActivityHook]]]
  val reactivities: mutable.Map[Performative, List[Reactivity]]

  protected[dima] def +[R <: ActivityHook](act: Proactivity[ActivityHook]) = act match {
    //TODO SORT!!!!
    case a: Reactivity => reactivities(a.m, reactivities(a.m) :: act)
    case _ => proactivities(act, proactivities(act) :: act)
  }

  /** ***
    * Execute the component proactivity   under a certain contexts defined by the
    * state, the mailbox and the knowledge
    */
  def apply() = state(id) match {


    //Si le composant est intérrompu, tentative de le réactivé
    case h: interrupted => {
      if (h.hook) {
        state(id) = continue()
      }
    }

    //Si le composant est actif
    case _: continue => {

      isInitialized match {

        //S'il n'est pas initializer on essaie les command d'intiailisation
        case notInitialized() => {
          isInitialized = initialized() //default initialization if all method are ok
          proactivities(proactivityInitialise()) foreach {
            m => if (m().equals(notInitialized)) isInitialized = notInitialized()
          }
        }

        //S'il est initialisé on execute sa proactivité
        case initialized() => {

          proactivities(preactivity()) foreach {
            m => m()
          }

          mailbox foreach {
            p => reactivities(p).foreach {
              m => m(p)
            }
          }

          proactivities(activity()) foreach {
            m => m()
          }

          proactivities(postactivity()).foreach {
            m => m()
          }

        }
      }
    }

    //Si le composant a terminer son execution
    case _: stopped => {

      if (isStopped.equals(notYet)) {
        isStopped = terminated()
        proactivities(proactivityTerminate()) foreach {
          m => if (m().equals(notYet())) isStopped = notYet()
        }
      }
    }
  }
}


/**
 * ProactivityPool :
 *
 * Pool of GreenTeaLeaf that encapsulate the execution of a set of commands
 *
 */

protected[dima] abstract class GreenTeaBush[S <: Core] extends mutable.List[GreenTeaLeaf[S]] {

   val proactivities = Map[ComponentIdentifier, GreenTeaLeaf[S]]
  /* */

  protected[dima] def +(comp: GreenTeaLeaf[S]) = apply(comp.id, comp)

  protected[dima] def apply(comp: ComponentIdentifier): GreenTeaLeaf[S] = ???

  def componentsIdentifiers: List[ComponentIdentifier] = ???

  /* */

  protected[dima] def doCycle =
    components foreach {
      c => c()
    }

}

trait Role extends Core

/**
 * GreenTea Language
 * Date: 6/29/13
 * Licensed under Lesser GNU Public General Licence (LGPL)
 * Author : Sylvain Ductor
 */
class GreenTeaTree[R <: Role] extends GreenTeaLeaf
with Map[R, List[AgentIdentifier]]
with Identification[Identifier]{

  type ActorContext

    //Requiert le contexte de chaque agent et
    def apply() : ConversationIdentifier
}

package commands {


//////////////////////////////////    //////////////////////////////////
// //////////////////////// GREENTEA Commands
// ///////////////////////////////   //////////////////////////////////


protected[dima] trait GreenTeaCommand[Available <: AvailableOptions]
  extends GreenTeaSeed {

  type OptionObject

  var options: Option[OptionObject] = None

}


protected[dima] trait GreenTeaOption extends Hook {

  type OptionParameters


  /**
   * Convert the mailbox and the knowledge into this specific activity parameter and execute "execute" function
   */
  def getParameters(): OptionParameters


}




protected[dima] class ProactivityCommand[Op <: AvailableOptions, +R <: ActivityHook](proact : Proactivity[ActivityHook] = proactivity)(implicit agent: Agent[Core])
  extends GreenTeaCommand[Op] with Comparable[ProactivityCommand[AvailableOptions, ActivityHook]] {


  def apply(option : GreenTeaOption) {

  }

  def newProactivity[Op2 <: AvailableOptions](prototype: ProactivityCommand[Op2, R]) {
    val p = new ProactivityCommand[Op2, R](this.systemPrecedence)
    p.options = this.options
    return p
  }

  /**
   * Used for  activity description
   * Defines the action and register the activity to the component
   * @param a : the associated action
   */
  def is(a: ActivityAction): proact.type = {
    action = Some(a)
    component + this
    this
  }


}


class PerformativeCommand[+I <: Identifier, Option <: AvailableOptions](val key: String = "") extends Performative with GreenTeaCommand[Option] {

  var reactionId: Option[GreenTeaLeaf[Core] => Proactivity]

  def apply() = {
    body.send(this)
  }

  /* Syntaxic sugar */
  def unary_send = apply()


}

protected[dima] trait NoOption extends GreenTeaOption

trait AvailableOptions


object proactivity {

  /* */
  /* InitialisationLoop */

  case class proactivityInitialise() extends ProactivityCommand[community.commands.ActivityOptions, InitializationHook](0)

  /* */


  /* */
  /* Activity Loop */

  case class preactivity() extends ProactivityCommand[community.commands.ActivityOptions, ExecutionHook](1)

  case class activity() extends ProactivityCommand[community.commands.ActivityOptions, ExecutionHook](2)

  case class postactivity() extends ProactivityCommand[community.commands.ActivityOptions, ExecutionHook](5)

  /* */


  case class proactivityTerminate() extends ProactivityCommand[community.commands.ActivityOptions, TerminationHook](6)


  /* Local */

  def reactionFor(p : Performative) : Either[p.ReturnType,GreenTeaException]

  case class delegate[T](agent : GreenTeaAgent, context : Promise[T]) extends GreenTeaCommand[DelegationOption] {
    def apply : Unit
  }
  //Les options (onSuccess : PartialFunction[T])(onError :  PartialFunction[T])

}


trait PerformativeOption extends AvailableOptions


}

