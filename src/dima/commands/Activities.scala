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
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTea. If not, see <http://www.gnu.org/licenses/>.
  * */
package dima.commands

import dima.speech.{PerformativeOption, Performative}
import dima._
import dima.activity.{ProactivityComponent, ActivityOption}
import dima.primitives.nativeCommandsOptionConfiguration._

/** ***
  * activities
  *
  * This object contains the commands used to define and control :
  * * the proactivty execution
  * * the events (messages) handling
  *
  */

object activities {

  import dima.activity._
  import dima.returns._


  /////////////
  // COMMANDS //
  /////////////


  /*
  * Activity
  */


  implicit val agent : GreenTeaAgent = null
  implicit val component : ProactivityComponent = null

  class ProactivityCommand[Op <: AvailableOptions, +R <: Return](val systemPrecedence: Int)(implicit agent : GreenTeaAgent, component : ProactivityComponent)
    extends Activity[R](agent, component) {
    type StateStatus = Any
    type ActivityParameters = Unit
    type OptionObject = ActivityOptionObject



    /**
     * Convert the mailbox and the knowledge into this specific activity parameter and execute "execute" function
     * @param mailbox
     */
    def getParameters(mailbox: List[Performative[PerformativeOption]], knowledge: dima.knowledge.KnowledgeBase): this.ActivityParameters = ???

  }


  /* */
  /* InitialisationLoop */

  class proactivityInitialise extends ProactivityCommand[InitialActivityAvailableOptions, Initialization](0)

  /* */


  /* */
  /* Activity Loop */

  class preactivity extends ProactivityCommand[InitialActivityAvailableOptions, Execution](1)

  class activity extends ProactivityCommand[InitialActivityAvailableOptions, Execution](4)

  class postactivity extends ProactivityCommand[InitialActivityAvailableOptions, Execution](5)

  /* */


  class proactivityTerminate extends ProactivityCommand[InitialActivityAvailableOptions, Termination](6)


  /*
  * Reactivity

  case class reactionFor[Message](p: MessageParser[AsyncMessage])
    extends MessageHandler[Activation, Message] {
    val priority = 2
  }

  case class handleOf[Message](p: MessageParser[SyncMessage])
    extends MessageHandler[Activation, List[Message]] {

    val priority = 3

    type Information = Message

    val parsedMessage: ListBuffer[Message]

    def update(mailBox: List[Message]): Boolean = {
      //parsedMessages += messages.filter(p)
      isReady(parsedMessage.toList)
    }

    def getMessages(): Option[List[Message]]
                             pour l'option list    (implicit isReady: List[Message] => Boolean)
  */

}
