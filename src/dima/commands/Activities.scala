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
package commands

object activities {

  import dima.activity._
  import dima.returns._


  /** ***
    * Proactivities
    *
    * This object contains the commands used to define and control :
    * * the proactivty execution
    * * the events (messages) handling
    *
    */

  /////////////
  // COMMANDS //
  /////////////


  /*
  * Activity
  */


  /* */
  /* InitialisationLoop */

  abstract class proactivityInitialise() extends InitialisationActivity[ActivityOption] {
    val systemPrecedence = 0
  }

  /* */


  /* */
  /* Activity Loop */

  abstract class preactivity() extends ExecutionActivity[ActivityOption] {
    val systemPrecedence = 1
  }

  abstract class activity() extends ExecutionActivity[ActivityOption] {
    val systemPrecedence = 4
  }

  abstract class postactivity() extends ExecutionActivity[ActivityOption] {
    val systemPrecedence = 5
  }

  /* */


  abstract class proactivityTerminate() extends TerminationActivity[ActivityOption] {
    val systemPrecedence = 6
  }


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
