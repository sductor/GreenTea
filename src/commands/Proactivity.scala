/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTea.

GreenTea is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GreenTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTea.  If not, see <http://www.gnu.org/licenses/>.
  * */

package commands

import commands.returns._
import dima._
import scala.collection.mutable.ListBuffer
import dima.{MessageHandler, MessageParser, ProactivityOption, Proactivity}


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
   * Proactivity
   */

  class ProactivityCommand[R <: Return](p : Int) extends Proactivity[R,ProactivityOption](new ProactivityOption()) {

    val priority = p
  }

  /* */
  /* InitialisationLoop */

  case class proactivityInitialise()
    extends ProactivityCommand(Initialisation, 0)

  /* */


  /* */
  /* Proactivity Loop */

  case class preactivity() extends Proactivity[Activation] {
    val priority = 1
  }

  case class activity() extends Proactivity[Activation] {
    val priority = 4
  }

  case class postactivity() extends Proactivity[Activation] {
    val priority = 5
  }

  /* */


  case class proactivityTerminate() extends Proactivity[Initialization] {
    val priority = 6
  }
}

  /*
   * Reactivity
   */
