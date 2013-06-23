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
package dima.primitives

import dima.{GreenTeaAgent, AvailableOptions, GreenTeaOption}
import dima.activity.{ActivityOption, ProactivityComponent}
import dima.primitives.ActivitiesOption.{WhenAvailable, WhenOptionObject}


/**
 * This object contains the native configuration for options
 */

object nativeCommandsOptionConfiguration {

  type ActivityOptionObject = GreenTeaOption with WhenOptionObject

  type InitialActivityAvailableOptions = AvailableOptions with WhenAvailable

  type InitialReactivityAvailableOptions = AvailableOptions

  type InitialPerformativeAvailableOptions = AvailableOptions

}



object ActivitiesOption {

  import dima.commands.activities.ProactivityCommand
  import dima.returns.Return

  implicit val agent : GreenTeaAgent = null
  implicit val component : ProactivityComponent = null

  /* When */

  type When = Boolean

  trait WhenOptionObject extends GreenTeaOption {
    var when : Option[When] = None
  }

  trait WhenAvailable extends AvailableOptions

  implicit class WhenOptionFilter[Opt <: AvailableOptions, T  <: Return](c: ProactivityCommand[Opt with WhenAvailable, T]) extends ActivityOption {

    def when(w: When): ProactivityCommand[Opt, T] = {
      c.options.when = Some(w)
      val p = new ProactivityCommand[Opt, T](c.systemPrecedence)      //et la precedence?? tout mettre dans les options!!!
      p.options = c.options
      p
    }

  }

}