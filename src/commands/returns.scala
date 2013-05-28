package commands

import scala.collection.mutable.ListBuffer

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

/** ***
  * Returns
  *
  *
  *
  *
  *
  *
  *
  *
  *
  *
  *
  *
  */
object returns {

  import clock.Hook
  import greentea._

  /*
   * Return Types
   */
  class Return extends GreenTea

  /*
  Initialization  activities
  */

  class Initialization extends Return

  case class initialized() extends Initialization

  case class notIinitialized() extends Initialization

  //implicit defs : boolean =| initialisationtype

  /*
   Proactive activities termination
   */

  class Activation extends Return

  case class continue() extends Activation

  case class stop() extends Activation

  case class interruptProactivity(hook: Hook) extends Activation

  case class interruptComponent(hook: Hook) extends Activation

}



