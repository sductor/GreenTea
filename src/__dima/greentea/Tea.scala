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

package dima.greentea

import dima._
import dima.dsl._
import dima.greentea.CascadeImplicits.Cascade
import dima.monitors._
import dima.platform._

import scala.collection.mutable
import scala.collection.immutable.TreeSet


//////////////////////////////////    //////////////////////////////////
// //////////////////////// GREENTEA IMPLEMENTATION
// ///////////////////////////////   //////////////////////////////////

/**
 * The GreenTeaSeed enrich the extended class with the three greentea objects used implicitly
 */
trait GreenTeaSeed extends GreenTeaObject {

  /**
   * agId is the identifier of the owner agent if any reference is needed
   */
  protected[dima] implicit val id: AgentIdentifier

  /**
   * body allows various operation :
   * sending message inside or outside the individual agent scope
   * accessing the agent mailbox
   * accessing the agent knowledge
   */
  protected[dima] implicit val body: GreenTeaBody

  /**
   * sage is the monitors of the agent
   * it allows an agent-centred exception handling
   */
  protected[dima] implicit val monitor: Sage

}

/**
 * GreenTeaLeaf :
 *
 * Is a GreenTeaSeed that has access to the agent core, it is to said, the shared object between agent component
 * The Leaf is to define a service or a whole proactivity
 * Thus it allows to define the initialisation, action, reaction and termination associated to the goal *
 */
trait GreenTeaLeaf[Core <: GreenTeaCore] extends GreenTeaSeed with Proactivities[Core] {

  ///////////////////////
  //////// GreenTeaAgent GreenTeaCore access
  //////////////////////

  protected[dima] val core: Core

  protected[dima] implicit val implicit_core: GreenTeaCore = core
  protected[dima] implicit val id: AgentIdentifier = core.id
  protected[dima] implicit val body: GreenTeaBody = core.body
  protected[dima] implicit val monitor: Sage = core.monitor
  protected[dima] implicit val leaf: GreenTeaLeaf[_] = this
  ///////////////////////  ///////////////////////
  //////// Component Description
  //////////////////////  ///////////////////////

  /**
   * the identification of this leaf as a service/goal-oriented behavior/...
   */
  protected[dima] implicit val compId: ComponentIdentifier = new ComponentIdentifier(this.getClass.getCanonicalName.toString)

  /**
   * Maps a type of proactivity (initialisation, execution, termination, ...)
   * with the set of autoplugged proactivity command of this leaf.
   * Sorted by user precedence defintion
   */
  protected[dima] val proactivities: mutable.Map[Proactivity[_], TreeSet[Proactivity[_]]] =
    new mutable.HashMap[Proactivity[_], TreeSet[Proactivity[_]]]

  /**
   * Maps a performative identifier the set of autoplugged reactivity command of this leaf.
   * Sorted by user precedence defintion
   */
  protected[dima] val reactivities: mutable.Map[Message, TreeSet[Reactivity[_]]] =
    new mutable.HashMap[Message, TreeSet[Reactivity[_]]]

  /**
   * Used by greentea command to "auto plug" a a proactivity
   * @param act : proactivity or reactivity to plug
   */
  protected[dima] def +=[OptionType <: GreenTeaOption, PluggedOptions <: OptionType]
  (act: GreenTeaCommand[OptionType, PluggedOptions]) = act match {
    case proact: Proactivity[_] => proactivities(proact) = (proactivities(proact) + proact)
    /* case react: Reactivity[_] => reactivities(react.body[key]) = (reactivities(react.body[key]) + react) */
    case _ => monitor.syntaxError()
  }


  ///////////////////////  ///////////////////////
  //////// DSL
  //////////////////////  ///////////////////////


  protected[dima] type ProactivityOptions <: dima.ProactivityOptions

  protected[dima] type ReactivityOptions <: dima.ReactivityOptions

  ///////////////////////
  //////// Proactivity Definition
  //////////////////////


  object proactivitiesExample {

    import proactivityoptions._

    val freetime: Boolean = true

    WhenSetter(preactivity()).when_>(freetime)

    ProactivityLoader(proactivityInitialise() when_> freetime) is {
      _ : Core => print("waking up!"); notInitialized()
    }

    /*   preactivity() when_> freetime is {
         _ => print("hello world!"); stop()
       }
       postactivity() when_> freetime is {
         _ : Core => print("goodnight world!");  stop()
       }     */

    preactivity() when_> freetime ticker_> low() is {
      c: Core => print("hello world!"); stop()
    }

    postactivity() when_> freetime ticker_> low() knowledge_> Nil is {
      _: (Core, Knowledge) => print("hello world!"); stop()
    }

    activity() when_> freetime ticker_> low() knowledge_> Nil is {
      _: (Core, Knowledge) => print("hello world!"); stop()
    }


    postactivity() when_> freetime ticker_> low()  messages_> {l => l} is {
      p: (Core, List[Message]) => print("hello world!"); print(p._2); stop()
    }

    postactivity() when_> freetime ticker_> low() knowledge_> Nil messages_> ({l => l},{l => l.hashCode() > 3}) is {
      p: (Core, List[Message], Knowledge) => val (c,ms,k) = p; print("goodnight world!"); print(ms); stop()
    }

  }

  ///////////////////////
  //////// Communication Definition
  //////////////////////

  import dima.dsl.performatives._

  def send[Return](message: GreenTeaPerformative[Return, _ <: SendActionOption]): Either[Return, GreenTeaException] = message send

  object performativesExample {

    val message = new GreenTeaPerformative[AsynchronousExecution, ACLOptions]()
    val m1: Message = message to_> "agent3" replyto_> "agent2"
    val m2: Message = inform("ho") to_> List("agent3", "agent2") replywith_> {
      _ => inform("hoyoyo")
    }

    val r2: Either[AsynchronousExecution, GreenTeaException] = send(message)
    val r4: Either[AsynchronousExecution, GreenTeaException] = !message
    val r6: Either[AsynchronousExecution, GreenTeaException] = message send

    val r3: Either[Int, GreenTeaException] = request[Int]("yo") to_> "agent3" replyto_> "agent2" send
    val r5: Either[Int, GreenTeaException] = !(request[Int]("yo") to_> "agent3" replyto_> "agent2")
    val r7: Either[Int, GreenTeaException] = send(request[Int]("yo") to_> "agent3" replyto_> "agent2")


    val m: Message = request[Int]("yo") replyto_> "agent2" inreplyto_> m2
    val r1 /*: Either[Int,GreenTeaException]*/ = !(request[Int]("yo") to_> "agent3" replyto_> "agent2" inreplyto_> m2)
    /*
  reactionFor(request[Int]("yo")) is {
    message => print(message.toString); (3, stop())
  }

  !m
  reactionFor(inform("yo") inreplyto_> m) is {
    message => print(message.toString); (3, stop())
  }   */
  }


  ///////////////////////
  //////// Proactivity Execution
  //////////////////////

  /**
   * Contains the current initialization status of this component
   */
  protected[dima] var internalStatus: Either[InitializationStatus, TerminationStatus] = Left(notInitialized())

  def restartFromInitialization = {
    internalStatus = Left(notInitialized())
  }

  def restart = {
    internalStatus = Left(initialized())
  }


  /** Execute the component proactivity
    * under a certain contexts defined by the state, the mailbox and the knowledge
    *
    * Firs parse the execution status defined by the core :
    * if the component is interrupted it tries to restart
    * if it is stopped it don't do anything and should be removed from the pool (interruption should be used if it might be manually restarted by the core)
    * else it parses its internal status
    * if it is not initialised it executes the initialisation methods
    * if it is terminanted
    */
  def apply = core.activity(compId) match {


    //Si le composant est intérrompu, tentative de le réactivé
    case h: interrupted => {
      if (h.isFreed) {
        core(compId) = continue()
      }
    }

    //Si le composant a terminer son execution
    case _: stop => {
      //TODO !!!! on se retire du bush
    }

    //Si le composant est actif
    case _: continue => {

      var context = new ContextParameter[Core, Nothing](core, ???, body.mailbox, body.knowledge)
      internalStatus match {

        //S'il n'est pas initializer on essaie les command d'intiailisation
        case Left(notInitialized()) => {
          internalStatus = Left(initialized()) //default initialization if all method are ok
          proactivities(proactivityInitialise()) foreach {
            m => if (m.execute.equals(notInitialized)) {
              internalStatus = Left(notInitialized())
            }
          }
        }

        //S'il est initialisé on execute sa proactivité
        case Left(initialized()) => {
          internalStatus = Right(terminating()) //default termination if every activity has stopped

          proactivities(preactivity()) foreach {
            m => if (!m.execute.equals(stop)) internalStatus = Left(initialized())
          }
          /*
        body.mailbox foreach {
          message: Message[_] => reactivities(message).foreach {
            m: Reactivity[_,_] =>
              ~message[replywith[m.key.ReturnType]] match {
              //appel faux : si replywith non instancié ca claque une exception il faudrait surchargé apply pour qui'l balance no option
              case None => m.execute
              case Some(rule) => //rule m.execute send
            }

          }
        }
            */
          proactivities(activity()) foreach {
            m => if (!m.execute.equals(stop)) internalStatus = Left(initialized())
          }

          proactivities(postactivity()).foreach {
            m => if (!m.execute.equals(stop)) internalStatus = Left(initialized())
          }

        }
        case Right(terminating()) => {
          internalStatus = Right(terminated()) //default termination if termination has stopped
          proactivities(proactivityTerminate()) foreach {
            m => if (m.execute.equals(terminating())) internalStatus = Right(terminated())
          }
        }


        case Right(terminated()) => {
          core(compId) = stop()
        }
      }
    }

  }
}

/**
 * Apply results on a state transition associated to methods execution that may affect the environnment
 */
trait GreenTeaBranch[C <: GreenTeaCore] extends Cascade[C] with Seq[Message]

object CascadeImplicits {

  implicit def toYourself[T](value: T): Yourself[T] = new Yourself(value)

  implicit def toCascade[T](value: T): Cascade[T] = new Cascade(value)

  class Yourself[T](value: T) {
    def sign = value
  }

  class Cascade[T](value: T) {
    def cascade[R1](
                     f1: T => R1) =
      f1(value)

    def cascade[R1, R2](
                         f1: T => R1,
                         f2: T => R2) = {
      f1(value)
      f2(value)
    }

    def cascade[R1, R2, R3](
                             f1: T => R1,
                             f2: T => R2,
                             f3: T => R3) = {
      f1(value)
      f2(value)
      f3(value)
    }

    def cascade[R1, R2, R3, R4](
                                 f1: T => R1,
                                 f2: T => R2,
                                 f3: T => R3,
                                 f4: T => R4) = {
      f1(value)
      f2(value)
      f3(value)
      f4(value)
    }

    def cascade[R1, R2, R3, R4, R5](
                                     f1: T => R1,
                                     f2: T => R2,
                                     f3: T => R3,
                                     f4: T => R4,
                                     f5: T => R5) = {
      f1(value)
      f2(value)
      f3(value)
      f4(value)
      f5(value)
    }
  }

}

/*
import CascadeImplicits._

class CascadeImplicitsExample {

class Turtle {
def clear = {}

def forward(v: Int) = {}

def turn(v: Int) = {}
}

val turtle = new Turtle

val res: Turtle =
turtle.cascade(
_.clear,
_.forward(10),
_.turn(90),
_.forward(20),
_.sign)

println(res)
*/


/**
 * ProactivityPool :
 *
 * Pool of GreenTeaLeaf that encapsulate the execution of a set of commands
 *
 */
protected[dima] trait GreenTeaBush[Core <: GreenTeaCore]
  extends GreenTeaObject
  //with mutable.ObservableMap[ComponentIdentifier, GreenTeaLeaf[Core]]
  with mutableMap[ComponentIdentifier, GreenTeaLeaf[Core]] {

  val leaves: mutable.Map[ComponentIdentifier, GreenTeaLeaf[Core]] = this.localMap

  /* */

  def componentsIdentifiers: Traversable[ComponentIdentifier] = this.keys

  protected[dima] def plug(comp: GreenTeaLeaf[Core]): ComponentIdentifier = {
    update(comp.compId, comp);
    comp.compId
  }

  protected[dima] def plug(comp: List[GreenTeaLeaf[Core]]) = {
    comp.foreach {
      c => update(c.compId, c)
    }
  }

  /* */

  protected[dima] def doCycle =
    foreach {
      a => val (_, c) = a; c
    }

}

trait Role extends GreenTeaCore

/**
 * Protocol
 */
trait GreenTeaTree[R <: GreenTeaCore] extends GreenTeaLeaf[R] /*with Protocol*/ {

  protected[dima] override type ProactivityOptions = dima.ProactivityOptions with RoleOption

  protected[dima] override type ReactivityOptions = dima.ReactivityOptions with RoleOption

  //Requiert le contexte de chaque agent et
  def apply(participants: Map[R, List[AgentIdentifier]]): ConversationIdentifier = ???


}











