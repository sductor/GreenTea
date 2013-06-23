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

package commands

import dima.commands.returns._
import dima._


/** ***
  * Proactivities
  *
  * This object contains the commands used to define and control :
  * * the proactivty execution
  * * the events (messages) handling
  *
  */
trait Proactivity extends GreenTeaSeed {


  /////////////
  // COMMANDS //
  /////////////


  /*
   * Activity
   */


  /* */
  /* InitialisationLoop */

  case class proactivityInitialise()
    extends Activity[ProactivityOption] {
    override type R = Initialization
    override val priority = 0
  }

  /* */


  /* */
  /* Activity Loop */

  case class preactivity() extends Activity[ProactivityOption] {
    override type R = Activation
    override val priority = 1
  }

  case class activity() extends Activity[ProactivityOption] {
    override type R = Activation
    override val priority = 4
  }

  case class postactivity() extends Activity[ProactivityOption] {
    override type R = Activation
    override val priority = 5
  }

  /* */


  case class proactivityTerminate() extends Activity[ProactivityOption] {
    override type R = Initialization
    override val priority = 6
  }

}

/*
 * Reactivity
 */
