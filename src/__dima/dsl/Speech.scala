package dima.dsl

 import dima._
import dima.monitors.{GreenTeaException, MessageTrace}
import dima.platform.GreenTeaBody
import scala.None


////////////////////////////
//// Message  Implementation
///////////////////////////

trait PerformativeOption extends  TypedKey with GreenTeaOption

/**
 * A performative allows to compose and to send a message
 *
 * param key Message are typically case class, they are thus filtered by the performative type and this "key" argument
 * tparam PluggedOption the agregation of the type of options that can still be used
 * tparam I : Type of the identifier of sender and receivers :
 * identify a communication canal and, thus, the communication component to use for sending
 */

/* */


abstract class Message extends Optionable
with TypedMap {

  type OptionType = PerformativeOption

  type IdentifierType

  def plug(o: PerformativeOption) = {
    this += o
  }

}

class Performative[+Return] extends Message

class GreenTeaPerformative[Return,Available <: PerformativeOption]
  extends Performative[Return] with GreenTeaCommand[PerformativeOption, Available] {
           self : Performative[Return] =>

  type BodyType =  Performative[Return]

  val body : BodyType= self

  type BuildType[T2 <: PerformativeOption] = GreenTeaPerformative[Return,T2]

  def build[T2 <: PerformativeOption]: BuildType[T2] = {
    val b = body
    new GreenTeaPerformative[Return,T2] {
      //override type BodyType = b.type
      override val body: BodyType = b
    }
  }
}





object performatives {

  /*
implicit def perfToCommand(p : Message) : GreenTeaCommand[PerformativeOption, BasicPerformativeOptions] =
 new GreenTeaPerformative[BasicPerformativeOptions]  */

  //////////////////////////// ////////////////////////////
  //// ////  ////  ////  Performatifs
  ///////////////////////////  ////////////////////////////

  class ACLPerformative[Return] extends GreenTeaPerformative[Return,ACLOptions]

  /*
* Interaction
*/


  case class inform(key: Any) extends ACLPerformative[AsynchronousExecution]

  case class request[R](key: Any) extends ACLPerformative[R]

  /*
* Log
*/


  case class log(message: String, details: String = "") extends GreenTeaPerformative[AsynchronousExecution,LogOptions]

  /*
* Log
*/


  case class acquaintance() extends GreenTeaPerformative[Identifier,AcquaintanceOptions]


  /*
* Faulty activities termination
*/

  case class warning(exTrace: Option[Throwable] = None, messTrace: Option[MessageTrace] = None, info: String = "")
    extends GreenTeaException(exTrace, messTrace, info)

  //Et log!!!


  case class notUnderstood(exTrace: Option[Throwable] = None, messTrace: Option[MessageTrace] = None, info: String = "")
    extends GreenTeaException(exTrace, messTrace, info)

  case class impossible(exTrace: Option[Throwable] = None, messTrace: Option[MessageTrace] = None, info: String = "")
    extends GreenTeaException(exTrace, messTrace, info)

  case class failure(exTrace: Option[Throwable] = None, messTrace: Option[MessageTrace] = None, info: String = "")
    extends GreenTeaException(exTrace, messTrace, info)


  //////////////////////////// ////////////////////////////
  //// ////  ////  ////  Options
  ///////////////////////////  ////////////////////////////

  ////////////////////////////
  //// sending
  ///////////////////////////

  trait SendActionOption extends PerformativeOption {
    type GET = Identifier
  }

  case class sender(val get: Identifier) extends SendActionOption

  implicit class PerformativeSender[Return,RemainingOption <: SendActionOption]
  (val command: GreenTeaPerformative[Return,RemainingOption] )(implicit body: GreenTeaBody, id: Identifier)
    extends CommandBuilder[PerformativeOption, RemainingOption, SendActionOption] {

    def send: Either[Return, GreenTeaException] = {
      def rec_handling(receivers: List[Identifier],
                       r: Option[Either[Return, GreenTeaException]])
      : Either[Return, GreenTeaException] =
        receivers match {
          case Nil => {
            body.order(command)
            return r.get
          }
          case id :: l if ((~command[sender].get) equals id) => rec_handling(l, Some(body.order(command)))
          case x :: l => {
            body.send(command, x);
            rec_handling(l, None)
          }
        }

      command[replyto] match {
        case None => command.plug(replyto(id))
        case Some(_) =>
      }

      consumeAll
      rec_handling(List(~command[to].get), None)
    }

    /* Syntaxic sugar */
    def unary_! : Either[Return, GreenTeaException] = send
    /* Syntaxic sugar */
    def send_:() : Either[Return, GreenTeaException] = send
    /* Syntaxic sugar */
    def unary_send : Either[Return, GreenTeaException] = send
  }

  ////////////////////////////
  //// receiver
  ///////////////////////////

  trait ReceiverNinReplyToOption extends PerformativeOption

  case class to(val get: Identifier) extends ReceiverNinReplyToOption {
    type GET = Identifier
  }

  case class inreplyto(val get: Option[Message]) extends ReceiverNinReplyToOption {
    type GET = Option[Message]
  }

  implicit class ReceiverSetter[Return,AvailableOption <: ReceiverNinReplyToOption](val command: GreenTeaPerformative[Return,AvailableOption])
    extends CommandBuilder[PerformativeOption, AvailableOption, ReceiverNinReplyToOption] {

    def to_>(r: Identifier) : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(to(r))
      command.plug(inreplyto(None))
      consume
    }

    def inreplyto_>(m: Message) : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(to(~m[replyto].get))
      command.plug(inreplyto(Some(m)))
      consume
    }
  }

  ////////////////////////////
  //// replyto
  ///////////////////////////

  trait ReplyToOption extends PerformativeOption {
    type GET = Identifier
  }

  case class replyto(val get: Identifier) extends ReplyToOption

  implicit class ReplyToSetter[Return,AvailableOption <: ReplyToOption](val command: GreenTeaPerformative[Return,AvailableOption])
    extends CommandBuilder[PerformativeOption, AvailableOption, ReplyToOption] {

    def replyto_>(r: Identifier) : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(replyto(r))
      consume
    }
  }


  ////////////////////////////
  //// replywith
  ///////////////////////////

  trait ReplyWithOption extends PerformativeOption {
    type ContentType
    type GET = Any => Message
  }

  case class replywith[R](val get: Any => Message) extends ReplyWithOption

  implicit class ReplyWithSetter[Return,AvailableOption <: ReplyToOption]
  (val command: GreenTeaPerformative[Return,AvailableOption])
    extends CommandBuilder[PerformativeOption, AvailableOption, ReplyToOption] {

    def replywith_>(rule : Any /*command.ReturnType*/ => Message) : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(replywith(rule))
      consume
    }
  }

  ////////////////////////////
  //// propagate
  ///////////////////////////

  trait PropagateOption extends PerformativeOption {
    type GET = Identifier => List[Identifier]
  }

  case class propagate(val get: Identifier => List[Identifier]) extends PropagateOption

  implicit class PropagateSetter[Return,AvailableOption <: PropagateOption]
  (val command: GreenTeaPerformative[Return,AvailableOption])
    extends CommandBuilder[PerformativeOption, AvailableOption, PropagateOption] {

    def propagate_>(rule : Identifier => List[Identifier]) : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(propagate(rule))
      consume
    }
  }

  ////////////////////////////
  //// proxy
  ///////////////////////////

  trait ProxyOption extends PerformativeOption {
    type GET = Boolean
  }

  case class proxy(val get: Boolean) extends ProxyOption

  implicit class ProxySetter[Return,AvailableOption <: ReplyToOption]
  (val command: GreenTeaPerformative[Return,AvailableOption])
    extends CommandBuilder[PerformativeOption, AvailableOption, ReplyToOption] {

    def toproxy_ : GreenTeaPerformative[Return,RemainingOptions] = {
      command.plug(proxy(true))
      consume
    }
  }
}

































/*
trait SimplestMessage
extends Optionable with Identification[MessageIdentifier] {

type OptionType

type Content
var content: Option[Content] = None

/* Particpant in communication */

var sender: Option[Identifier] = None

val replyTo: List[Identifier] = Nil

/* Control of conversation */

val id : MessageIdentifier = null
var protocol: Option[ProtocolIdentifier] = None
var conversationId: Option[ConversationIdentifier] = None

var replyWith: Option[Content => List[Message]] = None
var replyBy: Option[Date] = None

/* Meta-control of the communication */

var proxy: Option[Identifier] = receivers match {
case Nil => None
case x :: l => Some(x.proxy)
}
var propagate: Option[Identifier => List[Identifier]] = None

/* */


def plug(o: OptionType) {}
}


trait ACLMessage extends Message {

/* Description of Content */

var language: Option[LanguageIdentifier] = None;
var encoding: Option[EncodingIdentifier] = None;
var ontology: Option[OntologyIdentifier] = None;

}  */


//implicit def anyToActivation(v: Any): Activation = continue()

// object options {
/*
protected[dsl] class PerformativeBuilder(val p: Message)
extends LanguageBuilder[MessageContainer](p) {

/*
Exactement un des deux nécessaire
*/

def to(id: AgentIdentifier) {
p.obj.receiver = id
p.obj.conversationId = newConvId
}

def inreplyto(m: MessageContainer) {
p.obj.receiver = m.replyTo;
p.obj.inReplyTo = m;
assert(assertion = p.obj.protocol == m.protocol)
p.obj.conversationId = m.conversationId
p.obj.replyWith = m.replyWith
}

/*

*/

def languageIs(l: LanguageIdentifier) {
p.obj.language = l
}

def encodingIs(e: EncodingIdentifier) {
p.obj.encoding = e
}

def ontologyIs(o: OntologyIdentifier) {
p.obj.ontology = o
}


/*

*/

def proxy(id: AgentIdentifier) {
p.obj.proxy = id
}

def replyby(d: Date) {
p.obj.replyby = d
}

def replywith(mId: MessageIdentifier) {
p.obj.replywith = mId
}
}
*/
// }