
/**
 * Identification
 */

/* */

trait Identification[I <: Identifier] {

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
  * @param groupName
  */
proteceted[dima] class CoalitionIdentifier(val groupName: String, var proxy : Identifier, members : List[Identifier]  = Nil) extends mutable.List[Identifier](members)
 extends Query(proxy){

	
	private object numberOfGroupKnown { var n : Int = 0; def next : Int = { n++; n ; }
	
	def nextCoalitionName : String = "FROM--| proxy "+proxy+" #"+numberOfGroupKnown.next
	

}


trait CoalitionVisitor extends CoalitionIdentifier with Dynamic {
  
  implicit var o = Option[Visitor]
  
  case class state()(implicit o: Visitor) extends CoalitionQuery(this)

  protected[dima] case class stateField(val fieldName: String)(implicit o: Visitor) extends CoalitionQuery(this)

  def updateDynamic[(fieldName: String) : stateField = new stateField(fieldName)
  /* allows 
   * val id : AgentIdentifier; id.

  case class acquaintances()(implicit o: Visitor) extends CoalitionQuery(this)

  case class core()(implicit o: Visitor) extends CoalitionQuery(this)
  
  case class information(key: InformationIdentifier) extends CoalitionQuery(this)

  def info(key: InformationIdentifier)(implicit o: Visitor) = information(key, k)(o)
}


class AgentIdentifier(name : String) extends  CoalitionIdentifier(id, id, this)


/////////////////////////////////////////////////
///////////////////////////////////// OCommunication IDENTIFICATION
///////////////////////////////////////////////


class MessageIdentifier(cid: ConversationIdentifier, inreplyto : Option[MessageIdentifier], debug : SageTrace) extends Identifier

class ConversationIdentifier  extends Identifier


/////////////////////////////////////////////////
///////////////////////////////////// Internal component IDENTIFICATION
///////////////////////////////////////////////

class ComponentIdentifier(id: String) extends Identifier(id)
