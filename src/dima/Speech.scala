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
package dima

import returns._
import sage._

/* Messages */

class MessageContent extends GreenTeaObject

trait Performative [O <: PerformativeOption]
  extends GreenTeaCommand[MessageIdentifier] {

 // type O <: PerformativeOption

  type C <: MessageContent

  /* */

  val content: Option[C] = None

  val options: Option[O] = None

  /* sage */

  val inreplyto: Option[List[MessageTrace]]

  val internaltrace: GreenTeaThrowable

}

class PerformativeOption extends GreenTeaOption


/* */

abstract class ASyncPerformative[O <: PerformativeOption]()
  extends Performative[O] {

  /* a mettre dans asyncperf option */
  val sender: Identifier

  val receivers: List[Identifier]
  /* a mettre dans asyncperf option */

  def apply(agent: GreenTeaAgent[State]) = {
    agent.send(this)
  }

  /* Syntaxic sugar */
  def send(agent: GreenTeaAgent[State]) = {
    apply(agent: GreenTeaAgent[State])
  }
}

/* */

abstract class ASyncPerformativeWithReturn[O <: PerformativeOption, ReturnType <: Any]()
  extends ASyncPerformative[O]

/* */

abstract class SyncPerformative[O <: PerformativeOption, ReturnType <: Any]()
  extends Performative[O] {

  def apply(implicit agent: GreenTeaAgent[State]) : ReturnType = {
    agent.order(this)
  }

  /* Syntaxic sugar */
  def execute(agent: GreenTeaAgent[State]) = {
    apply(agent: GreenTeaAgent[State])
  }

}

/*
trait MessageHandlerOption extends ProactivityOption

trait MessageHandler[R <: Return, M <: Performative[PerformativeOption]]
  extends Activity[MessageHandlerOption]
  */