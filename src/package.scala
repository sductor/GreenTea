
import community.contexts.AkkaBody
import dima.greentea.{GreenTeaTree, GreenTeaLeaf}
import dima.speech.{Identification, AgentIdentifier, GreenTeaPerformative}
import java.util.Date
import scala.collection.parallel.mutable
import scala.util.Random

package object community {

  /**
   * Import language
   */

  import dima._

  /**
   * Import a platform
   */

  import contexts.akka._

  /**
   * Core
   */

  /**
   * The Core is the shared object between the internal component of an agent
   * It contains the raw values of the agent
   * And the decision function aims to be implemented in community.role
   *
   * Intents can be used to trigger component identifier (see Agent.execute())
   *
   * Basic state implementation
   * Reactive agent
   */
  class Core(seed : Long = 0) extends Core {

     val birthday : Date = new Date

    val random : Random = new Random(seed)

    /* Executed cyclicly before proactivity execution */
    def intentsUpdate {}

    /* Executed once at the agent initialization */
    def intentsInitiation {}
  }
     object simpleCore$ extends GreenTeaCore


  /**
   * Agent
   */

  /**
   * Reactive agent
   * @param name
   */
  class Artefact(name: String) extends Agent[GreenTeaCore](name, simpleCore$) {

    /* and some greentea ... */

    import nativecommands.proactivity._
    import nativecommands.performatives._

  }


  /**
   * Interaction specification between a mutable set of agent mapped to roles
   * @param conversationName : the instance conversation name
   * @tparam R : the role case classes used by the protocol
   */
  class Protocol[R <: Role](conversationName: String) extends GreenTeaTree[R](name) {

    /* and some greentea ... */

    import nativecommands.proactivity._
    import nativecommands.performatives._

    /* */

    import nativecommands.protocols._

  }

  /**
   * Communication component should be implemented as objects and imported
   * They will then transparently handle the  communication that has this I identifier type
   * @tparam I  : the Identifier associated to a communication canal that this object is adapted.
   */
  trait CommunicationComponent[I <: AgentIdentifier] extends GreenTeaObject {

    type P <: Performative[I]

    def send(m: P)

    def flushMail: List[P]
  }

  /**
   * Sources provides transparently asynchronous Knowledge
   * They are associated to a specific case tree of Query : Q
   * They should define this case tree and implement apply for every case of this tree
   * e.g.
   * @tparam Q : The key parsed by the map
   */

  trait Source[Q <: Query] extends GreenTeaLeaf with mutable.Map[Q, Information] with Identification[AgentIdentifier] {

    /* Return a knowledge */
    def apply(queries: List[Q]): Knowledge[Q]

    /* register the agents for the query */
    def apply(observer: AgentIdentifier, queries: List[Query], hook: Hook = always)
  }

  /* The request is used as the key of Source[SQL] */
  case class SQL(manager: AgentIdentifier, request: String) extends Query(manager)

}


