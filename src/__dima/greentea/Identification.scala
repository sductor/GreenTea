

package dima.greentea

import dima._
import scala.collection.mutable
import scala.language.dynamics
import dima.monitors.MessageTrace
                                                   \ite
/**
 * Identification
 */

/* */

/**
 * Enrich the extends class with an identification of type I
 * @tparam I
 */
trait Identification[I <: GreenTeaObjectIdentifier] {

  val id: I
}


/////////////////////////////////////////////////
///////////////////////////////////// /* CoalitionIdentifier */
///////////////////////////////////////////////


/** **
  * CoalitionIdentiifer is used as the basic identifier
  * Coalition should provide a unique name, and a proxy for communication
  * An agent identifier is simply represented as a singleton CoalitionIdentifier 
  *
  * String, List[String] et List[Identifier] are implicitly converted to CoalitionIdentifier with the first agent of the list as proxy
  *
An Identifier  associated to a unique string that represent a whole group
  *
  */
protected[dima] abstract class CoalitionIdentifier(val name: String, val members: Set[Identifier] = Set())
  extends mutable.HashSet[CoalitionIdentifier] with mutable.ObservableSet[CoalitionIdentifier] with GreenTeaObjectIdentifier {

  val proxy: Identifier

  this ++= members

  private object numberOfGroupKnown {
    var n: Int = 0;

    def next: Int = {
      n += 1;
      n;
    }
  }

  def nextCoalitionName: String = "FROM--| proxy " + proxy + " #" + numberOfGroupKnown.next

  type Pub = this.type
}

class CoalitionQuery(proxy: Identifier) extends Query(proxy)

class InformationIdentifier(proxy: Identifier) extends CoalitionQuery(proxy)

trait CoalitionVisitor extends CoalitionIdentifier with Dynamic {

  type Visitor

  implicit var o: Option[Visitor] = None

  case class state() extends CoalitionQuery(this)

  protected[dima] case class stateField(val fieldName: String) extends CoalitionQuery(this)

  def updateDynamic(fieldName: String): stateField = new stateField(fieldName)

  /* allows
   * val id : AgentIdentifier; id.unchamps
   * */

  case class acquaintances() extends CoalitionQuery(this)

  case class core() extends CoalitionQuery(this)

  case class information(key: InformationIdentifier) extends CoalitionQuery(this)

  def info(key: InformationIdentifier) = information(key)
}


class AgentIdentifier(name: String) extends CoalitionIdentifier(name) with CoalitionVisitor {

  val proxy: Identifier = this

}

class PrivateIdentifier[Core <: GreenTeaCore](override val name: String, agent: GreenTeaAgent[Core]) extends AgentIdentifier(name)

/////////////////////////////////////////////////
///////////////////////////////////// GreenTea Object IDENTIFICATION
///////////////////////////////////////////////
/*
Provides identification for important component. do not allow message sending
 */

trait GreenTeaObjectIdentifier extends GreenTeaObject

case class MessageIdentifier(cid: Option[ConversationIdentifier] = None, inreplyto: Option[MessageIdentifier] = None)
  extends GreenTeaObjectIdentifier with MessageTrace

case class ProtocolIdentifier() extends GreenTeaObjectIdentifier

case class LanguageIdentifier() extends GreenTeaObjectIdentifier

case class EncodingIdentifier() extends GreenTeaObjectIdentifier

case class OntologyIdentifier() extends GreenTeaObjectIdentifier

case class ConversationIdentifier() extends GreenTeaObjectIdentifier

case class ComponentIdentifier(id: String) extends GreenTeaObjectIdentifier
