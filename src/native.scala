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

////////////////////////////////////////////////////////////////
//////////////////////////// COMMANDS
////////////////////////////////////////////////////////////////


package native


import dima.{TerminationHook, ExecutionHook, InitializationHook, ProactivityCommand}



object performatives {

  import dima._

  /*
* Interaction
*/


  case class inform() extends ACLPerformative

  case class request() extends ACLPerformative

  /*
* Log
*/


  case class log(message: String)(implicit details: String) extends SyncPerformative

  /*
* Log
*/

  case class acquaintance() extends Performative[Sage]

  /*
* Faulty activities termination
*/
  case class warning(e: Exception, c: String) extends SagePerformative(e: Exception, c: String)


  abstract case class notUnderstood(e: Exception, c: String)
    extends ExceptionPerformative(e: Exception, c: String)

  abstract case class impossible(e: Exception, c: String)
    extends ExceptionPerformative(e: Exception, c: String)

  abstract case class failure(e: Exception, c: String)
    extends ExceptionPerformative(e: Exception, c: String)

  //implicit def anyToActivation(v: Any): Activation = continue()

}


object protocols {


}



////////////////////////////////////////////////////////////////
//////////////////////////// Options
////////////////////////////////////////////////////////////////


/**
 * This object contains the native configuration for options
 */

object options_conf {

  import ActivitiesOption._
  import ACLOptions._

  trait ActivityOption extends GreenTeaOption with TickerOption with WhenOption with KnowledgeOption 
  trait PerformativeOption extends GreenTeaOption with ACLOption 
 

  /* */

  type InitialActivityAvailableOptions = AvailableOptions with WhenAvailable

  type InitialPerformativeAvailableOptions = AvailableOptions

}



  //////////////////////////////////
  // ////////////////////////  Core Machine
  // ///////////////////////////////

  trait StateMachine extends GreenTeaCore

  type StateStatus

  var currentStatus : Option[StateStatus] = None


}

 object ACLOptions {


}




  class ActivityOptionParameters(s: Core, mails: List[Performative[PerformativeOption]], k: dima.knowledge.KnowledgeBase)

  /* Subtype for option used for activity commands */
  class ActivityOption extends GreenTeaOption {

    /**
     * @param mailbox: List[Performative[PerformativeOption], knowledge: knowledge.KnowledgeBase : the parameters that are to give to the action
     * @return  : wether this actual parameter are valide w.r.t to the option
     */
    type OptionParameters = ActivityOptionParameters


    /**
     * Core wether the action is executable with the provided option parameters
     * @param op
     * @return
     */
    def apply(op: ActivityOptionParameters): Boolean = ???
  }

  /* When */

  type When = Boolean

  trait WhenOptionObject extends GreenTeaOption {
    var when : Option[Boolean] = None
  }

  trait WhenAvailable extends AvailableOptions

  implicit class WhenOptionFilter[Opt <: AvailableOptions, R  <: Return]
  (c: ProactivityCommand[Opt with WhenAvailable, R])  {

    def when(w: Boolean): ProactivityCommand[Opt, R] = {
      c.options.when = Some(w)
      val p = new ProactivityCommand[Opt,R](c.systemPrecedence)(c.agent, c.component)
      p.options = c.options
      return p
    }

  }
