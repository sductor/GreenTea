/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  **/
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
  **/

package dima

trait Role extends greentea.State

//class Role[State]
class Protocol
extends greentea.GreenTeaSeed {

}

trait ProtocolCore extends greentea.GreenTeaComponent


import dima.Role
import dima.{Role, ConversationIdentifier}


         /*

class Protocol[State] extends greentea.GreenTeaComponent {

  def id : ProtocolIdentifier

  def play(rolesPlayed : List[Role[State]]) : Option[ConversationIdentifier]




}       */



