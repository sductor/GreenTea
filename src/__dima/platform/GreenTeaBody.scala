 /**
 * GreenTea Language
 * Date: 7/2/13
 * Licensed under Lesser GNU Public General Licence (LGPL)
 * Author : Sylvain Ductor
 */

package dima.platform

import dima._
import dima.dsl._
import dima.monitors.GreenTeaException
import dima.greentea.KnowledgeBase

/////////////////////////////////
//////////////////////////// GreenTeaBody
////////////////////////////////


/**
 * Provide an implicit access to
 * send, order
 * knowledge
 * mailbox
 *
 * This trait allows the agent to be executed by a platform
 * that implements the primitive actor operation for execution and communication
 *
 * It also provide meta component for linking to knowledge sources and using transparently different communication channel
 **/
trait GreenTeaBody extends GreenTeaObject {

  /* Communication asynchrone */

  def send(m: Message, id : Identifier)

  /* Communication synchrone */

  def order[R](m: Performative[R]): Either[R, GreenTeaException]


  /*Gestion de la mail box */

  def mailbox: List[Message]

  /*Gestion des connaissances */

  val knowledge: KnowledgeBase


  def start

  def update
}


      /*

  def act {
    var mailBox: List[Message] = _

    def updateMailBox(lb: ListBuffer[Message]) {
      reactWithin(1) {
        case msg: Message =>
          lb += msg
          updateMailBox(lb)
        case _ =>
          sage.communicationException(_)
          updateMailBox(lb)
      }
    }

    if (imAlive) {

      //core update
      updateHook
      val intents: List[Activity] = agent.state.generateIntents(this)

      //mailbox update
      val lb: ListBuffer[Message] = _updateMailBox(lb)
      mailBox = lb.toList

      //proaction
      intents.forall(_.execute(new ProactivityParameters(agent.state, mailBox, null, null))   )

      //loop
      if (imAlive) {
        act
      } else {
        //tproa activity terminate
      }
    }
  }
        */