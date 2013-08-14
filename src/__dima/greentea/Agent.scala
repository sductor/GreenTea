package dima.greentea

import dima._
import dima.dsl._
import scala.collection.mutable
import dima.monitors.{GreenTeaException, Sage}
import dima.platform._


////////////////////////////////////////////////////////////////
/////////////////////////////* GreenTeaCore */
////////////////////////////////////////////////////////////////

/**
 * The core is the shared object between all agents component
 * It is the root object, the one that can instanciate every other
 *
 * It first define the minimalistic agent that contains him
 * Note that this agent, as a GreenTeaBush, can be plugged with other proactivities
 *
 * It defines the agent identifier, its associated monitors and the platform implementation
 *
 * It is composed of a mutable state that contains the mutable information of the information
 * As a sharable, this state allows the definition of variable with control access
 *
 * Also it defines the decision function (Desires) of the agent that should be described in separate specific Role trait
 *
 * It can also access to the agent component list (as a GreenTeaBush)  and hold the current activity status  of each of it
 * Introspection allow to defines intents (Intentions) by conditionaly interrupting, stopping or restarting them
 *
 * Last it is prototypable :
 * One can save and load it into and from a dedicated format,
 * And produce a variation based on a branch
 *
 * @param agentName Name of the agent associated to thsi core
 * @param platform  The platform dependant primitives (send, receive, launch, migration etc)
 * @param sage Allows a meta control of the agent and in particular fault tolerance
 *             tparam State Option for mutable state
 */
abstract class GreenTeaCore(agentName: String)(implicit val platform: GreenTeaCore => GreenTeaBody, val sage: GreenTeaCore => Sage)
  extends GreenTeaSeed {

  ///////////////////////
  //  Agent Definition
  ///////////////////////

  type AgentType <: GreenTeaAgent[this.type]

  /* List of agent components */
  def components: List[ComponentIdentifier] = ???

  type State <: Sharable

  ///////////////////////
  //Seed definition
  ///////////////////////

  val id = new AgentIdentifier(agentName)

  val monitor = sage(this)

  val body = platform(this)

  ///////////////////////
  //Dynamic & Informative GreenTeaCore
  ///////////////////////

  var state: Option[State] = None

  //update the state
  def apply(branch: GreenTeaBranch[this.type]) = ???

  /** **
    * Execute reaction for associated method
    * */
  def apply[R](p: Performative[R]): Either[R, GreenTeaException] = body.order[R](p)

  ///////////////////////
  //Decisional GreenTeaCore
  ///////////////////////

  /* Etendu avec des trait Roles */

  /**
   * Statee if this request of protocol exection is accepted
   * @param p the request of a protocol execution
   * @return
   */
  def accept(p: GreenTeaTree[this.type]): Boolean

  ///////////////////////
  //Self-handled GreenTeaCore
  ///////////////////////

  /* Contains the activity status of component : updated by the state */
  private val hookedComponents: mutable.ListMap[ComponentIdentifier, Hook] = new mutable.ListMap[ComponentIdentifier, Hook]

  /* return component id current activity */
  def activity(id: ComponentIdentifier): ExecutionStatus = ???

  /* update component id current activity */
  def update(id: ComponentIdentifier, status: ExecutionStatus) = ???

  /*  Intents*/

  /* Executed once at the agent initialization */
  def intentsInitiation

  /* Executed cyclicly before proactivity execution */
  def intentsUpdate

  /* return the new activity status intented of the agent */
  def intentsActivity(): ExecutionStatus

  ////////////////////////////////////////////////////////////////
  ///////////////////////////// /* Protoyping */
  ////////////////////////////////////////////////////////////////

  def agent: GreenTeaAgent[this.type] = ???

  trait CoreSave[CoreType <: GreenTeaCore] extends GreenTeaObject

  def save: this.CoreSave[this.type]

  def create[CoreType <: GreenTeaCore](newAgentName: String, c: GreenTeaCore#CoreSave[CoreType]): CoreType

  def clone(newAgentName: String, branch: GreenTeaBranch[this.type]) = ???
}

/**
 * roots for state allows :
 * val yo : Shared[Int] = 3
 * var yo2 : Shared[String]=("yoo",only("agent3"))
 * @param agId
 */
class Sharable(implicit val agId: AgentIdentifier) {

  //Utilise id avec
  class Shared[+T](val value: T, val access: ControlAccess) extends Information {


    //Utilise
    def apply(): Option[T] = access(agId) match {
      case true => Some(value)
      case false => None
    }

  }

  /**
   * ControlAccess express a control of the access of an information for an agent
   *
   */
  sealed abstract class ControlAccess {

    //states whether agent id has access to the associated info
    def apply(id: Identifier): Boolean

  }

  //this default access put on a jvm-shared map
  case object everyone extends ControlAccess {

    def apply(id: Identifier): Boolean = true

  }

  case object none extends ControlAccess {

    def apply(id: Identifier): Boolean = false

  }

  case class only(ids: List[Identifier]) extends ControlAccess {

    def apply(id: Identifier): Boolean = ids contains id

  }

  case class excepted(ids: List[Identifier]) extends ControlAccess {

    def apply(id: Identifier): Boolean = !(ids contains id)

  }

  /** *
    * Automatically converted and handled type for state information sourcing
    *
    *
    * Shared state (informations are synchronized to a JVM-bounded map)
    * val publicKey : Shared[Int] = rand.nextLong()
    * val secret : Shared[Int] = ("the secret",only(this.id))
    * @param value
    * @tparam T
    */

  implicit def anyToShare[T](value: T): Shared[T] = new Shared[T](value, everyone)

  implicit def anyToShareWithCA[T](value: T, ca: ControlAccess): Shared[T] = new Shared[T](value, ca)
}

////////////////////////////////////////////////////////////////
//////////////////////////// GreenTeaAgent
////////////////////////////////////////////////////////////////

/** *
  * GreenTeaAgent is the central class. It is modelled as a container that centralise different objects
  * It is a GreenTeaLeaf and thus allow the implementation of actvities and communication using green tea commands
  * It is also a mutable pool of proactivities
  * * param agId Private identifier that has a reference on the agent, allows the production of every type of identifier
  * @param core  Shared object between the agent components allows state definition, intents control and decisions function definition
  * @tparam Core  core type
  */
class GreenTeaAgent[Core <: GreenTeaCore](val core: Core)
  extends GreenTeaLeaf[Core] with GreenTeaBush[Core] with Identification[PrivateIdentifier[Core]] {

  override val id: PrivateIdentifier[Core] = new PrivateIdentifier(core.id.name, this)

  /////////////////////////////////////////////
  //////////////////////////// ExecutionStatus
  /////////////////////////////////////////////
  /**
   * The loop execution of the agent is controlled by isActive
   * Once can apply to isActive :
   * * stop()
   * * continue()
   * * interrupt(h : Hookable)
   */
  var onOff: ExecutionStatus = stop()

  /** **
    * Allow to update the execution activity of an agent if one hold a reference
    * */
  def apply(newActivityStatus: ExecutionStatus) = {
    onOff = newActivityStatus
  }

  /** **
    * Execute reaction for associated method
    * */
  def apply[R](p: Performative[R]): Either[R, GreenTeaException] = core[R](p)

  /////////////////////////////////////////////
  //////////////////////////// Execution
  /////////////////////////////////////////////

  /* Launch the agent */
  def start {
    //bodies encapsulates a artefacts of execution, i.e.
    body.start
  }

  /* define its behavior */

  def execute {

    //starting
    onOff = continue()

    //allows core to initialize and initialize components activity
    core.intentsInitiation

    //agent runs while it has not been updated with (stopped())
    onOff match {
      //wait while hooked interruption is not finished
      case i: interrupted => {
        if (i.isFreed) onOff = continue()
        execute
      }

      //running mode
      case on: continue => {
        body.update
        core.intentsUpdate
        doCycle
        apply(core.intentsActivity)
        monitor.execute
        execute
      }

      case off: stop => {
        //stop
      }

    }
  }
}



