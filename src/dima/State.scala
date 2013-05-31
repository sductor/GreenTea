
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

package dima.state

import dima.GreenTea


object tools {

  type Problem = GreenTea {
    type Solution
  }


  trait Solver[P <: Problem] extends GreenTea {

    val p : Problem

    def getBest(): p.Solution

    def getNext(): p.Solution

    def getAll(): List[p.Solution]

  }


  type ChoiceSpecification = GreenTea {

    type Alternative
    type AlternativeSet

  }

  trait Decision[V <: ChoiceSpecification] extends Function1[V#AlternativeSet, V#Alternative]

}