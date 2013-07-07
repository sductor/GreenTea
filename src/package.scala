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

import community.core.NilCore

package object community {

  /**
   * Import language
   */

  import dima._

  /**
   * Import a platform
   */


  /**
   * Adapter
   * @param name
   */
  class Artefact(name: String) extends Agent[Core](name, NilCore)


  /**
   * Apply results on a state transition associated to methods execution that may affect the environnment
    */
  class Branch[C <: Core] extends GreenTeaObject with List[Performative[Identifier]]{

    def apply(core : C) : Unit = {
      //cascade
    }
  }

  class Contract extends GreenTeaObject with  Map[Identifier,Branch[Core]]


}


