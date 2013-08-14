
package dima.greentea

import dima._

/* */
////////////////////////////////////////////////////////////////
//////////////////////////// Knowledge
////////////////////////////////////////////////////////////////

/**
 * Query identify a information.
 * The agent that holds the information (the referee) must be provided
 *
 */
class Query(referee: Identifier) extends GreenTeaObjectIdentifier

trait Information //extends Identification[Query]

/** *
  * Knowledge build a map of information to answer a list of Queries
  * it returns true when the request are fullfilled
  */
case class Knowledge(queries: List[Query]) extends collection.mutable.HashMap[Query, Information] with Hook {

  def isFreed: Boolean = {
    this.size equals (queries.size)
  }
}

/**
 * A knowledge base allow agent to obtain hooks (i.e. Knowledge) about queries
 */
trait KnowledgeBase extends GreenTeaObject {

  def apply(query: Query): Option[Information] = ???

  def apply(queries: List[Query]): Knowledge = ???


}
