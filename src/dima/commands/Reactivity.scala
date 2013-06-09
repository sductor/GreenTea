
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
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTea.  If not, see <http://www.gnu.org/licenses/>.
  * */
package dima.commands

import dima.{MessageHandler, MessageParser, GreenTeaSeed}
import dima.commands.returns.Activation
import scala.collection.mutable.ListBuffer


trait Reactivity extends GreenTeaSeed {

  case class handle[Message](p: MessageParser[Message])
    extends MessageHandler[Activation, Message] {
    val priority = 2
  }

  case class handleList[Message](p: MessageParser[Message])
                                (implicit isReady: List[Message] => Boolean)
    extends MessageHandler[Activation, List[Message]] {

    val priority = 3

    type Information = Message

    val parsedMessage: ListBuffer[Message]

    def update(mailBox: List[Message]): Boolean = {
      //parsedMessages += messages.filter(p)
      isReady(parsedMessage.toList)
    }

    def getMessages(): Option[List[Message]]
  }

}