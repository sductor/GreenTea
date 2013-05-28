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
package dima

import greentea._
import sage._

/* Messages */


class PerformativeOption extends GreenTeaOption

trait Performative[O <: PerformativeOption]
  extends GreenTeaCommand[MessageIdentifier] {

  type M <: Message

  val message: M  = null


  val options: O = null


  def apply()   = {}

}


class MessageContent extends GreenTea

trait Message {

  type C <: MessageContent

  val id: MessageIdentifier

  val content: C

  /* sage */

  val inreplyto: Option[List[MessageTrace]]

  val internaltrace: GreenTeaThrowable
}


/* */

case class ASyncMessage()
  extends Message {

  val id: MessageIdentifier
  val sender: Identifier

  val receivers: List[Identifier]

  def apply(agent: Agent[State]) = {
    agent.send(this)
  }

  def send(agent: Agent[State]) = {
    apply(agent: Agent[State])
  }
}

/* */

case class SyncMessage[ReturnType <: Any]
  extends Message {

  def apply(implicit agent: Agent[State]) = {
    agent.order(this)
  }

  def execute(agent: Agent[State]) = {
    apply(agent: Agent[State])
  }

}

/* Commands */


case class SyncPerformative[O <: PerformativeOption, R <: Any]()
  extends  Performative[O] {
  type M=SyncMessage[R]
}


class MessageOption extends GreenTeaOption

class MessageParser[M <: Message] extends GreenTea

trait MessageHandler[R <: Return, M <: Message]
  extends Proactivity[R]

 class  AcquaintanceOption extends GreenTeaOption