import dima._
import scala.collection.mutable
import scala.Option
import java.util.Observable
import community.protocols.PublishSubscribeProtocol

/**
 * Knowledge
 */

/*
 * A KnowldgeDataBase
 * an object that extends either :
 * 		* KnowldgeDataBase[Query]
 *      * KnowldgeDataBaseDataBase[SynchronizedQuery]
 * and that is declared implicit in the scope of package dima
 *
 * It allows to retrieve Query.Information from Query
 * The query is done  either by using :
 * 	* The 'knowing' option of activity commands,
 * 		* followed by a list of Query.
 *      * The information will be given as argument
 *      * as a Map[Query,Information]
 *  * The application of a query(SynchronizedQuery) message
 *
 *
 */


/**
 * Knowledge
 */

/*
La classe query est le contenu d'un message d'accès a une information
Elle est retourné après que l'information demandé soit instancié
 */


trait Query {
  var referee: MessageReceiverIdentifier
}


class Information extends Cloneable {
  type Q <: Query
}

/*
Knowledge source observe itself and is able to produce
@Knowledge it is to said, li
 */
trait KnowledgeSource[Q <: Query] extends Observable
with Cloneable with PublishSubscribeProtocol {

  class Knowledge extends List[Q] {

    val hooks: List[Q] = List()

    def isReady(): Boolean = hooks.isEmpty

  }

  /*
  Self observation
   */

  val handledQueries: List[Q]

  //Auto publish mecanism
  val mySelfObserver: Observer = new Observer {
    def update(o: Observable, arg: Any) {
      /*on match l 'ancienne et la nouvelle valeur pour chaque handledQueries
        et on publie les queries modifié
        publish(...); */
    }
  }

  this.addObserver(mySelfObserver)


  //return a complete knowledge if all info are known or
  //a hooked knowledge if not
  def getKnowledge(queries: List[Q]): Knowledge = {

    var k: Knowledge = _

    queries.foreach {
      q =>
        if (q.referee.equals(agent.id))
          k += getLocalKnowledge(q)
        else
          subscribe(q) to q.referee
        k += addHook(q)
    }

  }

  def getLocalKnowledge(q: Q): Information //assert q \in handledQueries

}

object KnowledgeSource


object MetaKnowledgeDataBase extends ListMap[Query, Information] {

  class DoubleAccessQueriers {
    val queriers: mutable.ListMap[Query, Set[Proactivity]]
    val reverseQueriers: mutable.ListMap[Proactivity, Set[Query]]

    def addQuerier(q: Query, p: Proactivity) = {}

    //Return true if the query subscription must be stop
    def removeQuerier(q: Query, p: Proactivity): Boolean = {}

    def contains(q: Query): Boolean = queriers.contains(q)
  }

  val currentQueriers = new DoubleAccessQueriers


  def getKnowledge(q: Query, p: Proactivity): Option[q.Information] = {
    var r: Option[q.Information] = _
    if (this.contains(q)) {
      r = this.get(q)
    } else {
      if (!currentQueriers.contains(q)) {
        subscribe(q) to q.referee()
      }
      r = None
    }
    currentQueriers.addQuerier(q, p)
    return r;
  }

  def stopSubscription(q: Query, p: Proactivity) {
    currentQueriers.removeQuerier(q, p)
  }
}


//subscribe automatique aux leaders étranger pour les update des group identifier


/**
 * Les objet de type KnowledgeDB fournissent de facon transparente un acces par référence, locale ou distante,
 * à des ensembles d'information
 */
/*
class KnowledgeDataBase extends Protocol {
//implemente l'observer du pattern observer

def generateKnowledge[Query](List

[Query] ) ( implicit

object agent

: GreenTea)
( implicit

object kdb

: KnowledgeDataBase[Query]: Knowledge[Query.Information] {

val k = new Knowledge[Query.Information]
val subscribed: List[Query]

this.foreach (q =| {
if (q.infoIsKnown () ) {
k += q.information
k.hook += q.infoIsKnown ()

if (! subcribed.contains q)
subscribe (q) to q.owner ()
else
queryRef (q) to q.owner ()
}
})
}
}   */


/** native state knowledge hanfling */
/*

class StateQuery(val id: Identifier) extends Query {

type Information = AgentInformation

class AgentInformation(var fieldName: String) extends Information {
type Value = Any
}

} */





