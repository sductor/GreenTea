package dima.dsl


import dima._
import dima.greentea._
import dima.monitors.{GreenTeaException, Sage}
import dima.platform.GreenTeaBody
import scala.Some

/**
 *
 * This files contains the basic components for activity description and execution
 */


//////////////////////////////////    //////////////////////////////////
// //////////////////////// Options and builder definition
// ///////////////////////////////   //////////////////////////////////

/** ActivityOption is a GreenTeaOption type that relies on some parameters that allows to define the isFreed methods herited from hook.
  * It hence allows a asynchronous control of the command execution.
  *
  * It also defines an execution context that it needs to implements
  */

protected[dima] trait ActivityOption extends GreenTeaOption with TypedKey with Hook

protected[dima] class ContextParameter[Core <: GreenTeaCore, P <: Message]
(val core: Core, val p: P, val mailbox: List[Message], val knowledge: KnowledgeBase)


protected[dsl] trait ActivityOptionable extends Optionable with TypedMap {

  /* Option definition */

  type OptionType = ActivityOption

  //val options: mutable.Set[OptionType] = new mutable.HashSet[OptionType]()

  def plug(opt: OptionType) {
    this += opt
  }

}

//////////////////////////////////    //////////////////////////////////
// //////////////////////// Command
// ///////////////////////////////   //////////////////////////////////


protected[dsl] abstract class ActivityCommand[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: ActivityOption](implicit sage: Sage)
  extends GreenTeaCommand[ActivityOption, AvailableOptions] with ActivityOptionable {
  self: ActivityOptionable =>

  type BodyType = ActivityOptionable

  val body: BodyType = self

  type BuildType[Available2 <: ActivityOption] = ActivityCommand[Parameters, Return, C, P, Available2]

  def build[Available2 <: ActivityOption] = this match {
    case p: Proactivity[Return] =>
      val b = body
      new ActivityCommand[Parameters, Return, C, P, Available2] with Proactivity[Return] {
        def getParameters(context: ContextParameter[C, P]) = self.getParameters(context)

        override val body: BodyType = b
      }
    case r: Reactivity[Return] =>
      val b = body
      new ActivityCommand[Parameters, Return, C, P, Available2] with Reactivity[Return] {
        def getParameters(context: ContextParameter[C, P]) = self.getParameters(context)

        override val body: BodyType = b
      }
    case _ => sage.syntaxError()
  }

  def buildWithParam[NeoParam, Available2 <: ActivityOption]
  (params: ContextParameter[C, P] => NeoParam): ActivityCommand[NeoParam, Return, C, P, Available2] = this match {
    case p: Proactivity[Return] =>
      val b = body
      new ActivityCommand[NeoParam, Return, C, P, Available2] with Proactivity[Return] {
        def getParameters(context: ContextParameter[C, P]): NeoParam = params(context)

        override val body: BodyType = b
      }
    case r: Reactivity[Return] =>
      val b = body
      new ActivityCommand[NeoParam, Return, C, P, Available2] with Reactivity[Return] {
        val key = b[perf]

        def getParameters(context: ContextParameter[C, P]): NeoParam = params(context)

        override val body: BodyType = b
      }
    case _ => sage.syntaxError()
  }

  /* Execution Control */


  def set[AT <: ((Parameters) => Return)](a: AT) = body += actionContent(a).typedAs[ActionOption]

  /*
def getParameters(context: ContextParameter[C, P])(implicit extractor: ParameterExtractor[Parameters]): Parameters =
extractor.apply(context, body)  */

  def getParameters(context: ContextParameter[C, P]): Parameters

  def isFreed: Boolean = body.forall[Hook](o => o.isFreed)

  var context: ContextParameter[C, P] = ???

  /**
   * Used to execute the method
   * @return  : the new activty status of this activity
   */
  def execute(implicit sage: Sage): Either[Return, AsynchronousExecution] =
    body[ActionOption] match {
      case Some(a) => if (isFreed) {
        try {
          Left(a.asInstanceOf[actionContent[Parameters, Return]].get(getParameters(context)))
        } catch {
          case e: Throwable => sage.error(e)
        }
      } else {
        Right(AsynchronousExecution)
      }
      case None => sage.syntaxError()
    }

}

trait ActivityBuilder[Parameters, Return, C <: GreenTeaCore, P <: Message, AOs <: OptionToConsume, OptionToConsume <: ActivityOption]
  extends CommandBuilder[ActivityOption, AOs, OptionToConsume] {

  override val command: ActivityCommand[Parameters, Return, C, P, AOs]

  def consumeP[NeoParam](params: ContextParameter[C, P] => NeoParam) : ActivityCommand[NeoParam, Return, C, P, RemainingOptions]=
    command.buildWithParam[NeoParam, RemainingOptions](params: ContextParameter[C, P] => NeoParam)

}

protected[dima] trait Proactivity[Return] extends TypedMap {
  def execute(implicit sage: Sage): Either[Return, AsynchronousExecution]
}

protected[dima] trait Reactivity[Return] extends TypedMap {
  def execute(implicit sage: Sage): Either[Return, AsynchronousExecution]
}


//////////////////////////// ////////////////////////////
//// ////  ////  ////  Activity Command
///////////////////////////  ////////////////////////////

trait Proactivities[C <: GreenTeaCore] extends GreenTeaSeed {

  protected[dima] type ProactivityOptions <: dima.ProactivityOptions

  protected[dima] type ReactivityOptions <: dima.ReactivityOptions


  trait BasicProactivityCommand[Return, C <: GreenTeaCore, AvailableOptions <: ActivityOption]
    extends ActivityCommand[C, Return, C, Message, AvailableOptions] with Proactivity[Return] {

    def getParameters(context: ContextParameter[C, Message]): C = context.core
  }


  trait BasicReactivityCommand[Return, C <: GreenTeaCore, P <: Performative[Return], AvailableOptions <: ActivityOption]
    extends ActivityCommand[(C, P), Return, C, P, AvailableOptions] with Reactivity[Return] {

    val key : P

    body.plug(perf(key))

    def getParameters(context: ContextParameter[C, P]): (C, P) = (context.core, context.p)
  }


  /* */
  /* InitialisationLoop */

  case class proactivityInitialise()
    extends BasicProactivityCommand[InitializationStatus, C, ProactivityOptions]


  /* */


  /* */
  /* Activity Loop */

  case class preactivity()
    extends BasicProactivityCommand[ExecutionStatus, C, ProactivityOptions]

  case class activity()
    extends BasicProactivityCommand[ExecutionStatus, C, ProactivityOptions]

  case class postactivity()
    extends BasicProactivityCommand[ExecutionStatus, C, ProactivityOptions]

  /* */


  case class proactivityTerminate()
    extends BasicProactivityCommand[TerminationStatus, C, ProactivityOptions]


  /* Local */

  case class reactionFor[Return, P <: Performative[Return]](val key: P)
    extends BasicReactivityCommand[Return, C, P, ProactivityOptions]


  // case class delegate(agent: GreenTeaAgent, plan: Planner) extends DelegationCommand[InitializationStatus](-1)
  //Les options (onSuccess : PartialFunction[T])(onError :  PartialFunction[T])


  /*
val freetime: Boolean = true
//  ProactivityLoader(WhenSetter(preactivity()).when_>(freetime)) is
proactivityInitialise() when_> freetime is {
  _: Unit => print("waking up!"); notInitialized()
}  */
}


//////////////////////////// ////////////////////////////
//// ////  ////  ////  Options
///////////////////////////  ////////////////////////////

trait KnowledgeOption extends ActivityOption {
  type GET = Knowledge

  def isFreed: Boolean = get.isFreed
}

case class knowledge(get: Knowledge) extends KnowledgeOption

/* */

trait MailsOption extends ActivityOption {
  type GET = List[Message] => List[Message]
  val sufficient: List[Message] => Boolean
  val b: GreenTeaBody

  def isFreed: Boolean = sufficient(b.mailbox)
}

case class messages(get: List[Message] => List[Message], val sufficient: List[Message] => Boolean)(implicit val b: GreenTeaBody) extends MailsOption

/* */

trait ActionOption extends ActivityOption {
  def isFreed: Boolean = true
}
case class actionContent[Parameters, Return](get: Parameters => Return) extends ActionOption {
  type GET = Parameters => Return
}

/* */

case class perf(get: Message) extends ActivityOption {
  type GET = Message

  def isFreed: Boolean = true
}

///////////////

trait TransparentActivityOption extends ActivityOption {

  type GET = Nothing
  protected val get = ??? //OK
}

trait FrequenceOption extends TransparentActivityOption

trait FaultHandlingOption extends TransparentActivityOption

trait WhenOption extends TransparentActivityOption

trait RoleOption extends TransparentActivityOption

///////////////

object proactivityoptions {

  ////////////////////////////
  //// ////  ////  ////  knowledge
  ///////////////////////////

  abstract class KnowledgeQuerier[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: KnowledgeOption, NeoParams]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])(implicit b: GreenTeaBody)
    extends ActivityBuilder[Parameters, Return, C, P, AvailableOptions, KnowledgeOption] {

    def knowledge_>(queries: List[Query]) = {
      plug(knowledge(b.knowledge(queries)))
      myConsume
    }

    def myConsume : ActivityCommand[NeoParams, Return, C, P, RemainingOptions]
  }

  implicit class KnowledgeQuerierProact[Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: KnowledgeOption]
  (override val command: ActivityCommand[C, Return, C, P, AvailableOptions]) (implicit  b: GreenTeaBody)
    extends KnowledgeQuerier[C, Return, C, P, AvailableOptions, (C, Knowledge)](command){

    def myConsume = consumeP[(C, Knowledge)]({context : ContextParameter[C,P]=>
      (context.core, command[knowledge].get.get) })
  }

  implicit class KnowledgeQuerierReact[Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: KnowledgeOption]
  (override val command: ActivityCommand[(C, P), Return, C, P, AvailableOptions])(implicit  b: GreenTeaBody)
    extends KnowledgeQuerier[(C, P), Return, C, P, AvailableOptions,(C, P, Knowledge)](command){

    def myConsume = consumeP[(C, P, Knowledge)]({context : ContextParameter[C,P] =>
      (context.core, context.p, command[knowledge].get.get) })
  }

  implicit class KnowledgeQuerierProactL[Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: KnowledgeOption]
  (override val command: ActivityCommand[(C, List[Message]), Return, C, P, AvailableOptions])(implicit  b: GreenTeaBody)
    extends KnowledgeQuerier[(C, List[Message]), Return, C, P, AvailableOptions,(C, List[Message], Knowledge)](command){


    def myConsume = consumeP[(C, List[Message], Knowledge)]({context : ContextParameter[C,P] =>
      (context.core,  command[messages].get.get(context.mailbox), command[knowledge].get.get) })
  }

  ////////////////////////////
  //// ////  ////  ////  mail analysis
  ///////////////////////////

  abstract class MailsFilter[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: MailsOption, NeoParams]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])   (implicit b: GreenTeaBody)
    extends ActivityBuilder[Parameters, Return, C, P, AvailableOptions, MailsOption]{

    def all = {l : List[Message] => l}

    def always = {l : List[Message] => true}

    def messages_>(filter: List[Message] => List[Message]=all, sufficient: List[Message] => Boolean=always) = {
      plug(messages(filter,sufficient))
      myConsume
    }

    def myConsume : ActivityCommand[NeoParams, Return, C, P, RemainingOptions]
  }


  implicit class MailsFilterProact[Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: MailsOption]
  (override val command: ActivityCommand[C, Return, C, P, AvailableOptions])  (implicit b: GreenTeaBody)
    extends MailsFilter[C, Return, C, P, AvailableOptions, (C, List[Message])](command) {

    def myConsume =  consumeP[(C, List[Message])]({context : ContextParameter[C,P] =>
      (context.core,  command[messages].get.get(context.mailbox))})
  }

  implicit class MailsFilterProactK[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: MailsOption]
  (override val command: ActivityCommand[(C, Knowledge), Return, C, P, AvailableOptions])(implicit b: GreenTeaBody)
    extends MailsFilter[(C, Knowledge), Return, C, P, AvailableOptions, (C, List[Message], Knowledge)] (command) {

    def myConsume =  consumeP[(C, List[Message], Knowledge)]({context : ContextParameter[C,P] =>
      (context.core,  command[messages].get.get(context.mailbox), command[knowledge].get.get) })
  }

  ////////////////////////////
  //// ////  ////  ////  is
  ///////////////////////////

  implicit class ProactivityLoader[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: ActionOption]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])(implicit leaf: GreenTeaLeaf[_])
    extends CommandBuilder[ActivityOption, AvailableOptions, ActionOption] {

    /**
     * Used for  activity description
     * Defines the action and register the activity to the component
     * @param a : the associated action
     */
    def is[AT <: ((Parameters) => Return)](a: AT) = {
      command.set[AT](a)
      leaf += command
      resetTo[ActivityOption with FaultHandlingOption]
    }
  }

  ////////////////////////////
  //// ////  ////  ////  when
  ///////////////////////////


  implicit class WhenSetter[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: WhenOption]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])
    extends CommandBuilder[ActivityOption, AvailableOptions, WhenOption]
    with WhenOption {

    var w: () => Boolean = ??? // ??? is ok

    def when_>(w: () => Boolean) = {
      this.w = w
      plug(this)
      consume
    }

    def isFreed: Boolean = w()
  }

  ////////////////////////////
  //// ////  ////  ////  catch
  ///////////////////////////

  implicit class FaultHandler[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: FaultHandlingOption]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])
    extends CommandBuilder[ActivityOption, AvailableOptions, FaultHandlingOption]
    with FaultHandlingOption {

    var exHand: GreenTeaException => Either[Nothing, GreenTeaException] = ??? // ??? is ok

    def when_>(exHand: GreenTeaException => Either[Nothing, GreenTeaException]) = {
      this.exHand = exHand
      plug(this)
      consume
    }

    def isFreed: Boolean = true
  }

  ////////////////////////////
  //// ////  ////  ////  /* Frequence */
  ///////////////////////////


  implicit class FrequenceSetter[Parameters, Return, C <: GreenTeaCore, P <: Message, AvailableOptions <: FrequenceOption]
  (val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])
    extends CommandBuilder[ActivityOption, AvailableOptions, FrequenceOption]
    with FrequenceOption {

    var t: Ticker = ??? //??? ok

    def ticker_>(t: Ticker) = {
      plug(this)
      consume
    }

    def isFreed: Boolean = t.isFreed
  }

  ////////////////////////////
  //// ////  ////  ////  /* Role */
  ///////////////////////////
  /*

implicit class RoleConstrainer[Parameters, Return, C, P, AvailableOptions <: RoleOption]
(val command: ActivityCommand[Parameters, Return, C, P, AvailableOptions])
 extends CommandBuilder[ActivityOption, AvailableOptions, RoleOption] {


 case class role(val r: Role)(implicit val core: GreenTeaCore) extends RoleOption {

   def isFreed(): Boolean = {
     core.isInstanceOf[r.type]
   }
 }

 def role_>(r: Role) = {
   plug(role(r))
   consume
 }
}
     */
}
