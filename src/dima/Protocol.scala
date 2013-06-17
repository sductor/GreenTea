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

package dima

import dima._
import scala.collection.mutable

trait Role extends State

abstract class Conversation extends GreenTeaObject with Identification[ConversationIdentifier] with mutable.MultiMap[Role,AgentIdentifier]

abstract class Protocol(val rolesPlayed : List[Role])  extends GreenTeaSeed with Identification[ProtocolIdentifier]{

  import commands.protocols._

 def startConversation(acq : GroupIdentifier) : Conversation


}





