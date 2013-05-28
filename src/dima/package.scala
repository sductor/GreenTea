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
import scala.collection.mutable.ListBuffer

package object dima {

  import commands._

  /* */

  trait GreenTea
    extends Cloneable with Serializable


  /* */

  trait GreenTeaArtefact[Id <: ArtefactIdentifier]
    extends GreenTeaSeed[Id] with Speech with Reactivity with Protocols {

    protected[greentea] implicit val agent : Agent[State]

  }


  /* */


  trait GreenTeaSeed[Id <: Identifier]
    extends GreenTea with Identified[Id] {

    protected[greentea] implicit val agent : Agent[State]

    }

  /* */

  trait GreenTeaOption
    extends GreenTea

  trait GreenTeaCommand[GreenTeaOption]
    extends GreenTeaArtefact[CommandIdentifier]




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
  type State

  //with Source[State]
  // extends GreenTeaArtefact[StateIdentifier]

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
  = GreenTeaSeed[AgentIdentifier]
    with GreenTeaComponent
    with knowledge.Source[GreenTeaComponent] {


    /* */

    val id: AgentIdentifier

    /* */
    type  StateType = S
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

    type GreenTeaClock = greentea.GreenTeaArtefact[ClockIdentifier] {

      val intialDate: java.util.Date
    }


    type HookTest = Unit => Boolean

    trait Hook extends greentea.GreenTea {

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

    type Source[I <: Information] = GreenTeaArtefact[SourceIdentifier]

    type KnowledgeBase = {

      def apply(queries: List[Query]): ListBuffer[Information] with clock.Hook
    }


    /** * **
      * Query et Info
      */
    trait Query extends GreenTea {

      val referee: Identifier

    }



    type Configuration[Type] = {

      val agent: Agent[State]

      def apply(): Type

    }
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

    class ExceptionOption extends greentea.GreenTeaOption

    class ExceptionMessage(val ex: Exception, val cause: String)
      extends Message with Performative[ExceptionOption]

    class LogMessage extends  Message with Performative[LogOption]

    class LogOption extends greentea.GreenTeaOption


  }
}
