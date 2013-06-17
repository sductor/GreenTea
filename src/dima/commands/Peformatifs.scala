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
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with GreenTea. If not, see <http://www.gnu.org/licenses/>.
  * */

package commands.performatives

import dima.speech._
import dima._
import dima.sage.{ExceptionPerformative}

  /*
* Interaction
*/

class ACLOption extends PerformativeOption

  abstract case class inform() extends Performative[ACLOption]

abstract case class request() extends Performative[ACLOption]

  /*
* Log
*/


abstract case class log() extends sage.LogPerformative

abstract case class warning(e: Exception, c: String) extends ExceptionPerformative(e: Exception, c: String)


abstract case class acquaintance() extends Performative[AcquaintanceOption]

  /*
* Faulty activities termination
*/

abstract case class notUnderstood(e: Exception, c: String)
  extends ExceptionPerformative(e: Exception, c: String)

abstract case class impossible(e: Exception, c: String)
  extends ExceptionPerformative(e: Exception, c: String)

abstract  case class failure(e: Exception, c: String)
  extends ExceptionPerformative(e: Exception, c: String)

  //implicit def anyToActivation(v: Any): Activation = continue()
