

/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTeaObject.

GreenTeaObject is isFreed software: you can redistribute it and/or modify
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


import dima.greentea.{GreenTeaCore, GreenTeaBranch}
import dima.monitors._
import community.cores.Core
import dima._
import dima.greentea._
import dima.platform.GreenTeaBody


package object community {


  abstract class Contract extends GreenTeaObject with Map[Identifier, GreenTeaBranch[Core]]

  ////////////////////////////////////////////////////////////////
  //////////////////////////// Identification
  ////////////////////////////////////////////////////////////////

  //lourd et nimp???

  /*
   * String, Set[String] et Set[Identifier] can be now used as native Identifier
   */

  ////////////////////////////////////////////////////////////////
  //////////////////////////// Execution Context
  ////////////////////////////////////////////////////////////////

  implicit def platform(c : GreenTeaCore): GreenTeaBody = new AkkaBody

  implicit def monitor(c : GreenTeaCore): Sage = new Sage
}

/*
////////////////////////////////////////////////////////////////
//////////////////////////// Options
////////////////////////////////////////////////////////////////


/**
* This object contains the native configuration for options
*/



trait ActivityOptions extends GreenTeaOption with TickerOption with WhenOption with KnowledgeOption

trait PerformativeOption extends GreenTeaOption with ACLOption


/* */

type InitialActivityAvailableOptions = AvailableOptions with WhenAvailable

type InitialPerformativeAvailableOptions = AvailableOptions


//////////////////////////////////
// ////////////////////////  GreenTeaCore Machine
// ///////////////////////////////

//trait StateMachine extends GreenTeaCore

//type StateStatus = Any

//var currentStatus : Option[StateStatus] = None


//}   //

// ACLOptions


class ActivityOptionParameters(s: GreenTeaCore, mails: List[Message[PerformativeOption]], k: dima.knowledge.KnowledgeBase)

/* Subtype for option used for activity commands */
/*  class ActivityOption extends GreenTeaOption {

/**
* @param mailbox: List[Message[PerformativeOption], knowledge: knowledge.KnowledgeBase : the parameters that are to give to the action
* @return  : wether this actual parameter are valide w.r.t to the option
*/
type OptionParameters = ActivityOptionParameters


/**
* GreenTeaCore wether the action is executable with the provided option parameters
* @param op
* @return
*/
def apply(op: ActivityOptionParameters): Boolean = ???
} */

}

/* When

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

*/
 */

