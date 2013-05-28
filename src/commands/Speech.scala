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

import dima._
import dima.sage.{LogOption, ExceptionMessage}
import community.communicationcomponents.ACLOption


trait Speech extends Agent {



  /*
   * Interaction
   */

  case class inform() extends Performative[ACLOption]

  case class request() extends Performative[ACLOption]

  /*
   * Log
   */


  case class log() extends Performative[LogOption]

  case class warning() extends Performative[LogOption]

  case class acquaintance() extends Performative[AcquaintanceOption]

  /*
   * Faulty activities termination
   */

  case class notUnderstood(m: Message)
                          (implicit ex: Exception, cause: String)
    extends ExceptionMessage(ex, cause)

  case class impossible(m: Message)
                       (implicit ex: Exception, cause: String)
    extends ExceptionMessage(ex, cause)

  case class failure(m: Message)
                    (implicit ex: Exception, cause: String)
    extends ExceptionMessage(ex, cause)

  //implicit def anyToActivation(v: Any): Activation = continue()
}

