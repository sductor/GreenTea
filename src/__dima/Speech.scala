package dima.speech

import dima._
import dima.{Proactivity, Identifier}
import dima.Knowledge.Query
import dima.greentea._
import javax.management.Query

////////////////////////////////////////////////////////////////
////////////////////////////                 /* Speech*/
////////////////////////////////////////////////////////////////

/* */

/* Messages */

/* */

abstract class Performative[+I <: Identifier]()
  extends GreenTeaPerformative[I] {

  val id: MessageIdentifier = new MessageIdentifier

  var receiver: Option[List[Identifier]] = None
  /* a mettre dans asyncperf option */
  val sender: Identifier

  val receivers: List[Identifier]
  /* a mettre dans asyncperf option */

  def apply() = {
    body.send(this)
  }

  /* Syntaxic sugar */
  def send() = apply()
}

/* */

abstract class InternalPerformative[ReturnType <: Any]()
  extends Performative[PrivateIdentifier] {

  def apply(): ReturnType = {
    body.order(this)
  }

  /* Syntaxic sugar */
  def execute() = apply()

}
