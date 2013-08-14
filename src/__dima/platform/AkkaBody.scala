
import dima.dsl.{Performative, Message}
import dima.monitors.GreenTeaException
import dima.greentea._
import dima._
import dima.platform.GreenTeaBody

class AkkaBody extends GreenTeaBody{

  val knowledge: KnowledgeBase = ???

  def mailbox: List[Message] = ???

  /*   */

  def send(m: Message, id : Identifier) {}

  def order[R](m: Performative[R]): Either[R, GreenTeaException] = ???

  /*   */

  def start {}

  def update {}

}