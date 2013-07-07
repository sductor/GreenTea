

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
import dima.sage._
import dima.greentea.body._
import dima.greentea._
import dima.identifiers._

import scala.collection.mutable
import scala.Some
 package object dima {



   trait GreenTeaObject
     extends Serializable with Cloneable


   ////////////////////////////////////////////////////////////////
   //////////////////////////// Identification
   ////////////////////////////////////////////////////////////////

	//Most General Identifier
	trait Identifier extends  CoalitionIdentifier

/*
 * Implicit conversion of String, List[String] et List[Identifier] to Identifier
 */

implicit def stringToIDentifier (id: String): Identifier =
new AgentIdentifier(id)

implicit def idToList[I <: Identifier] (ids:  List[String]) : Identifier = 
new CoalitionIdentifier (ids.head.nextCoalitionIdentifier, ids.head, members = ids)

implicit def idToList[I <: Identifier] (ids:  List[I]) : Identifier = 
new CoalitionIdentifier (ids.head.nextCoalitionIdentifier, ids.head, members = ids)



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
     */
   class Agent[C <: Core](id: PrivateIdentifier, core: C)(implicit b: Body, m: Sage)
     extends GreenTeaLeaf[C] with GreenTeaBush[C] with Identification[PrivateIdentifier] {

     val body: Body = b.build
     val monitor: Sage = m.build

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
     var onOff: ExecutionStatus = stopped()

     /** **
       * Allow to update the execution activity of an agent if one hold a reference
       * */
     def apply(newActivityStatus: ExecutionStatus) = {
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


   }

   implicit def interrupt (h: Hook) : interrupted.type = {
     val i = interrupted ()
     i.h = h
     i
   }


   ////////////////////////////////////////////////////////////////
   /////////////////////////////* Context */
   ////////////////////////////////////////////////////////////////
   /**
    * To be implemented has object
    * @param b : Platform adapter
    * @param s : Fault-tolerance layer
    */
   class ExecutionContextFactory(b: Body, s: Sage) {

     implicit val body: Body = b
     implicit val monitor: Sage = s

   }

   implicit def exceptionToGreenTea(ex: Exception) = new GreenTeaException(Some(ex))(ex.cause())

   ////////////////////////////////////////////////////////////////
   /////////////////////////////* Core */
   ////////////////////////////////////////////////////////////////


   trait Core extends GreenTeaObject {

     //Dynamic Core

     def apply(neo: Core) = ???

     // Informative Core

     /**
      * ControlAccess express a control of the access of an information for an agent
      *
      */
     sealed abstract class ControlAccess {

       //cores whether agent id has access to the associated info
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

     implicit def anyToShare[T](value: T): Shared[T] = new Shared[T](value, everyone)

     implicit def anyToShareWithCA[T](value: T, ca: ControlAccess): Shared[T] = new Shared[T](value, ca)


     //Decisional Core

     /* Etendu avec des trait Roles */

     //

     def accept(p : GreenTeaTree) : Boolean = true



     //Auto-contrôle

     /* Contains the activity status of component : updated by the state */
     private val hookedComponents: mutable.ListMap[ComponentIdentifier, Hook] = new mutable.ListMap[ComponentIdentifier, Hook]

     /* List of agent components */
     def components: List[ComponentIdentifier] = ???

     /* return component id current activity */
     def activity(id: ComponentIdentifier): ExecutionStatus = ???

     /* update component id current activity */
     def update(id: ComponentIdentifier, status: ExecutionStatus) = ???

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


   trait CoreSave extends GreenTeaObject

   def agent[S <: Core, A <: Agent[S]] (id: AgentIdentifier, spec: TypeTag[A], state: S) = ???

   def state (s: CoreSave): Core

   def save (s: Core): CoreSave

   ////////////////////////////////////////////////////////////////
   ///////////////////////////// /* Speech */
   ////////////////////////////////////////////////////////////////



   /* Messages */

   /* */

   type GreenTeaPerformative = GreenTeaSeed  {

     val messId: MessageIdentifier = new MessageIdentifier

   }

   trait Performative[+I <: Identifier]  extends GreenTeaPerformative {

     type ReturnType

     var receiver: Option[List[Identifier]] = None

     val sender: Identifier

     val receivers: Option[List[Identifier]]

     /* Syntaxic sugar */
     def apply(): Option[ReturnType] = {
       receivers match {
         case None => body.order(this)
         case Some(id) if id.equals(this.id) => body.order(this)
         case _ => body.send(this)
       }
     }
   }
 }
