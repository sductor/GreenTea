   /**
GreenTea Language
Copyright 2013 Sylvain Ductor
  **/
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
  **/

   import commands.Protocols
   import dima.commands._
   import dima.state.State
   import scala.collection.mutable.ListBuffer

package object dima {

  import commands._

  /* */

  trait GreenTea
    extends Cloneable with Serializable


  /* */

  trait GreenTeaArtefact[Id <: ArtefactIdentifier]
    extends GreenTeaSeed  with Reactivity with Protocols with Identification[Id] {

    protected[dima] implicit val agent : Agent[State]

  }


  /* */


  trait GreenTeaSeed extends GreenTea {

    protected[dima] implicit val agent : Agent[State]

    import dima.commands.Speech._

    }

  /* */

  trait GreenTeaOption
    extends GreenTea

  trait GreenTeaCommand[GreenTeaOption]
    extends GreenTeaArtefact[CommandIdentifier]


  /** * Private identifieir
    *
    */

  type Key = Long
  type Random = java.util.Random

  /**
   * Speech
   */


       type ACLOption = MessageOption

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

  implicit def stringToAgentIdentifier(s: String): AgentIdentifier = new AgentIdentifier(s);

  type Agent[+S <: State]
  = GreenTeaSeed with Identification[AgentIdentifier]
    with GreenTeaComponent[S]
    with knowledge.Source[GreenTeaComponent[S]]
    with knowledge.Source[S] {


    /* */

    val id: AgentIdentifier

    /* */
    type  MyStateType = S
    val state: S


    /* */

    def send(m: ASyncMessage)

    /* */

    def order[R](m: SyncMessage[R]): R

    /* */

    val myClock: clock.GreenTeaClock

    /* */

    val mySage: sage.GreenTeaSage

  }
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

    type GreenTeaClock = dima.GreenTeaArtefact[ClockIdentifier] {

      val intialDate: java.util.Date
    }


    type HookTest = Unit => Boolean

    trait Hook extends dima.GreenTea {

      var isHooked: Boolean = false

      def test: HookTest

      def reactivate(): Boolean = {
        if (isHooked) {
          isHooked = test ()
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

    type Source[I <: Information] = GreenTeaArtefact[SourceIdentifier] {

      //val sources : Set[I]

    }

    type KnowledgeBase = {

      def apply(queries: List[Query]): ListBuffer[Information] with clock.Hook
    }


    /** * **
      * Query et Info
      */
    trait Query extends GreenTea {

      val referee: Identifier

    }
  }



  type Configuration[Type] = {

    val agent: Agent[State]

    def apply(): Type

  }

  object sage {

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
      def syntax(gt: ExceptionMessage)

      /*
      Method executed if an execution error is detected
       */
      def execution(gt: ExceptionMessage)

      /*
      Method executed if a protocol error is detected
       */
      def communication(em: ExceptionMessage)

      /*

       */

      def log(lm: LogMessage)
    }

    /*

     */

    type MessageTrace = GreenTeaThrowable

    class ExceptionOption extends dima.GreenTeaOption

    class ExceptionMessage(val ex: Exception)(implicit val cause: String)
      extends Message with Performative[ExceptionOption]

    class LogMessage extends  Message with Performative[SyncMessageOption]


  }
}
