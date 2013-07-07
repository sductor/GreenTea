import dima.speech._
import dima.speech.CommandIdentifier
import dima.speech.ComponentIdentifier
import greentea.{GreenTeaBush, GreenTeaLeaf}
import scala.collection.mutable
import scala.Some

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

 package object dima {



   trait GreenTeaObject
     extends Serializable with Cloneable


   ////////////////////////////////////////////////////////////////
   //////////////////////////// Identification
   ////////////////////////////////////////////////////////////////

	//Most General Identifier
	type Identifier = CoalitionIdentifier

/*
 * Implicit conversion of String, List[String] et List[Identifier] to Identifier
 */

implicit def stringToIDentifier (s: String): Identifier = 
new CoalitionIdentifier(id, id)

implicit def idToList[I <: Identifier] (ids:  List[String]) : Identifier = 
new CoalitionIdentifier (ids.head.nexrCoalitionIdentifier, ids.head, members = ids)

implicit def idToList[I <: Identifier] (ids:  List[I]) : Identifier = 
new CoalitionIdentifier (ids.head.nexrCoalitionIdentifier, ids.head, members = ids)



	type Hook = GreenTeaSeed with Identification[Identifier] {
	
		def free : Unit => Boolean
		
	}


/*
 * String, Set[String] et Set[Identifier] can be now used as native Identifier 
 */

   ////////////////////////////////////////////////////////////////
   //////////////////////////// Agent
   ////////////////////////////////////////////////////////////////

   /** *
     *
     * @param id
     * @param state
     * @param List
     * @tparam S
     */
   class Agent[C <: Core](val id: Identifier, core: C)(implicit body: Body, sage: Sage)
     extends GreenTeaLeaf[S] with GreenTeaBush[S] with Identification[AgentIdentifier] {


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
     var onOff: ExecutionHook = stopped()

     /** **
       * Allow to update the execution activity of an agent if one hold a reference
       * */
     def apply(newActivityStatus: ExecutionHook) = {
       activity = newActivityStatus
     }

     /////////////////////////////////////////////
     //////////////////////////// Execution
     /////////////////////////////////////////////

     /* Launch the agent */
     def start {
       //body encapsulates a context of execution, i.e. 
       body.start()
     }

     /* define its behavior */

     def execute {
	   
	   //starting
       activity = continue()
       
       //allows core to initialize and initialize components activity
       core.intentsInitiation
       
       //agent runs while it has not been updated with (stopped())
       while (!activity.equals(stopped())) {
       
		 //wait while hooked interruption is not finished	
         if (activity.equals(interrupted()) && activity.hook) {
           activity = continue()
         } 
         
         //running mode
         else if (activity.equals(continue())) {
           body.update
           core.intentsUpdate
           doCycle
           apply(state.agentActivity())
           sage.execute
         }
       }
     }

    /* and some greentea ... */

    import community.native.performatives._
    import community.native.options._


   }


   /**
    * To be implemented has object
    * @param b : Platform adapter
    * @param s : Fault-tolerance layer
    */
   class ExecutionContextFactory(b: Body, s: Sage) {

     implicit val body: Body = b
     implicit val sage: Sage = s

   }

   ////////////////////////////////////////////////////////////////
   /////////////////////////////* Core */
   ////////////////////////////////////////////////////////////////


   trait Core extends GreenTeaObject {

     //Dynamic Core

     def apply(neo: Core)

     // Informative Core

     /**
      * ControlAccess express a control of the access of an information for an agent
      *
      */
     sealed abstract class ControlAccess {

       //cores whether agent id has access to the associated info
       def apply(id: AgentIdentifier): Boolean

     }

     //this default access put on a jvm-shared map
     case object everyone extends ControlAccess {

       def apply(id: AgentIdentifier): Boolean = true

     }

     case object none extends ControlAccess {

       def apply(id: AgentIdentifier): Boolean = false

     }

     case class only(ids: List[AgentIdentifier]) extends ControlAccess {

       def apply(id: AgentIdentifier): Boolean = ids contains id

     }

     case class excepted(ids: List[AgentIdentifier]) extends ControlAccess {

       def apply(id: AgentIdentifier): Boolean = !(ids contains id)

     }

     /** *
       * Automatically converted and handled type for state information sourcing
       *
       *
       * Shared state (informations are synchronized to a JVM-bounded map)
       * val publicKey : Shared[Int] = rand.nextLong()
       * val secret : Shared[Int] = ("the secret",only(this.id))
       * @param value
       * @param access
       * @param id
       * @tparam T
       */

     //Utilise id avec
     class Shared[+T](val value: T, val access: ControlAccess)(implicit val id: AgentIdentifier) extends Information {

       //Utilise
       def apply(): Option[T] = access(id) match {
         case true => Some(value)
         case false => None
       }

     }

     implicit def anyToShare[T](value: T): Shared[T] = new Shared[T](value, new everyone)

     implicit def anyToShareWithCA[T](value: T, ca: ControlAccess): Shared[T] = new Shared[T](value, ca)


     //Decisional Core

     /* Etendu avec des trait Roles */

     def accept(p : GreenTeaTree) : Boolean = true


     //Auto-contrôle

     /* Contains the activity status of component : updated by the state */
     private val hookedComponents: mutable.ListMap[ComponentIdentifier, Hook] = new mutable.ListMap[ComponentIdentifier, Hook]

     /* Contains the activity status of commands : updated by the command itself */
     private val hookedMethods: mutable.ListMap[CommandIdentifier, Hook] = new mutable.ListMap[CommandIdentifier, Hook]

     /* List of agent components */
     def components: List[ComponentIdentifier] = ???

     /* return component id current activity */
     def apply(id: InternalIdentifier): ExecutionHook = ???

     /* update component id current activity */
     def update(id: InternalIdentifier, status: ExecutionHook) = ???

     /*  Intents*/

     /* Executed once at the agent initialization */
     def intentsInitiation

     /* Executed cyclicly before proactivity execution */
     def intentsUpdate


     // Prototyping

     def clone()

   }

   ////////////////////////////////////////////////////////////////
   ///////////////////////////// /* Protoyping */
   ////////////////////////////////////////////////////////////////


   trait StateSave extends GreenTeaObject

   def agent[S <: State, A <: Agent[S]] (id: AgentIdentifier, spec: Manifest[A], state: S) = ??? //Remplacer manifest par typetag

   def state (s: StateSave): State

   def save (s: State): StateSave



 }
