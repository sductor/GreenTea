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

import dima.activity.ProactivityComponent
import dima.Identification
import dima.speech._
import scala.collection.mutable.ListBuffer

package object dima {

  /* AKKA ALIASES */

  type Identifier = GreenTeaObject

  class SimpleIdentifier(s: String) extends GreenTeaObject

  implicit def stringToIDentifier(s: String): Identifier = new SimpleIdentifier(s)

  /* GREENTEA ALIASES */

  type Reactivity = GreenTeaObject //berk


  type Protocols = Set[Reactivity]

  //berk

  /* GreenTea Implem */
  /* */

  trait GreenTeaObject
    extends Cloneable with Serializable


  /*

  trait GreenTeaArtefact[Id <: ArtefactIdentifier]
    extends GreenTeaSeed with Reactivity with Protocols with Identification[Id] {

    protected[dima] implicit val agent: GreenTeaAgent[State]

  }
  */

  /* */

  trait GreenTeaSeed extends GreenTeaObject {

    protected[dima] val agent: GreenTeaAgent

    def state(): agent.MyStateType = agent.state

    def updateState(newState: agent.MyStateType) = agent.update(newState)

    import dima.commands.performatives._

  }


  /* */
  /**
   * The following classes allow the use of commands with extensible options
   * Each command is endowed with an object that contains all the optionnal fields that can be added
   * It is associated to a current AvailableOptions that allow to identify which option can still be used
   */

  /* */

  trait GreenTeaCommand
    extends GreenTeaSeed with Identification[CommandIdentifier] {

    type OptionObject <: GreenTeaOption

    protected[dima] var options: Option[OptionObject] = None
  }


  trait GreenTeaOption extends GreenTeaObject {

    type OptionParameters

    /**
     * State wether the action is executable with the provided option parameters
     * @param op
     * @return
     */
    def apply(op: this.OptionParameters): Boolean

  }


  trait AvailableOptions


  /** * Private identifieir
    *
    */

  type Key = Long
  type Random = java.util.Random

  /**
   * Speech
   */

  type Query = GreenTeaObject

  type Knowledge = GreenTeaObject

  //type ACLOption = PerformativeOption

  /** *****
    * State
    *
    *
    *
    *
    *
    *
    *
    */
  // type State = {val yo : Int}//knowledge.Source[State]
  // extends GreenTeaArtefact[StateIdentifier]

  class State

  /** * **
    * Agent
    *
    *
    *
    *
    *
    */

  implicit def identifierToAgentIdentifier(s: Identifier): AgentIdentifier = new AgentIdentifier(s);

  trait GreenTeaAgent extends  Identification[AgentIdentifier] {

    /* */

    type MyStateType <: State

    var state: MyStateType

    def update(newState: MyStateType): Unit

    /* */

    val myClock: clock.GreenTeaClock

    /* */

    def send[O <: PerformativeOption](m: ASyncPerformative[O])


    def order[O <: PerformativeOption, R](m: SyncPerformative[O, R]): R

    /* */


    def obtainKnowledge(queries : List[Query]) : Knowledge

    /* */

    val mySage: sage.GreenTeaSage

  }


  /*
 type Configuration[Type] = {

   val agent: Agent[State]

   def apply(): Type

 }
    */
  /** * **
    * Artefact
    *
    *
    *
    *
    *
    */

  object clock {

    type Ticker = {

      def isActivated
    }

    type GreenTeaClock = dima.GreenTeaObject {

      val intialDate: java.util.Date
    }


    type HookTest = Unit => Boolean

    trait Hook extends dima.GreenTeaObject {

      var isHooked: Boolean = false

      def test: HookTest

      def isReactivated(): Boolean = {
        if (isHooked) {
          isHooked = test()
        }
        isHooked
      }
    }

  }

  object knowledge {


    type Information = Serializable with Cloneable {

      val id: Query

    }

    type Knowledge = List[Information]

    type Source[I <: Information] = GreenTeaObject with Identification[SourceIdentifier] {

      //val sources : Set[I]

    }

    type KnowledgeBase = {

      def apply(queries: List[Query]): ListBuffer[Information] with clock.Hook
    }


    /** * **
      * Query et Info
      */
    trait Query extends GreenTeaObject {

      val referee: Identifier

    }

  }


  object sage {
    //http://tersesystems.com/2012/12/27/error-handling-in-scala

    type GreenTeaThrowable
    = Throwable with clock.Hook


    type GreenTeaRepair
    = {

      type G <: GreenTeaThrowable

      val id: G

    }

    type GreenTeaSage
    = List[GreenTeaRepair] {

      /*
      Method executed if a syntax error is detected
       */
      def syntax(gt: ExceptionPerformative)

      /*
      Method executed if an execution error is detected
       */
      def execution(gt: ExceptionPerformative)

      /*
      Method executed if a protocol error is detected
       */
      def communication(em: ExceptionPerformative)

      /*

       */

      def log(lm: LogPerformative)
    }

    /*

     */

    type MessageTrace = GreenTeaThrowable

    abstract class ExceptionOption extends dima.speech.PerformativeOption

    abstract class LogOption extends dima.speech.PerformativeOption

    abstract class ExceptionPerformative(val ex: Exception, val cause: String)
      extends Performative[ExceptionOption]

    abstract class LogPerformative extends Performative[LogOption]


  }


  object returns {

    /*
     * Return Types
     */
    class Return extends GreenTeaObject

    case class interrupted() extends Return with Initialization with Execution with Termination {
      var hook: clock.Hook = _
    }

    def interrupt(hook: clock.Hook): interrupted = {
      val i = new interrupted()
      i.hook = hook
      i
    }

    /*
    Initialization  activities
    */

    trait Initialization extends Return

    case class initialized() extends Initialization

    case class notInitialized() extends Initialization

    //implicit defs : boolean =| initialisationtype

    /*
     Proactive activities
     */

    trait Execution extends Return

    case class continue() extends Execution

    case class stop() extends Execution

    /*
     Proactive activities termination
     */

    trait Termination extends Return

    case class terminate() extends Termination

    case class notYet() extends Termination


  }


  object acquaintances {


    class AcquaintanceOption extends PerformativeOption

    //ajout de handleOf de acquaintances
  }

}