package dima.dsl

import dima._

//////////////////////////////////    //////////////////////////////////
/** ***
  * This file describes the GreenTea command system
  */
// ///////////////////////////////   //////////////////////////////////

//////////////////////////////////    //////////////////////////////////
// //////////////////////// GREENTEA Language
// ///////////////////////////////   //////////////////////////////////
/** *
  * Optionable is the object produced by a command
  * Such an object is pluggable with  so called GreenTeaOption s
  * T : This optionable object can only be plugged with option of type T
  */
protected[dima] trait Optionable extends GreenTeaObject {

  type OptionType <: GreenTeaOption

  def plug(o: OptionType)

}

/**
 * Mother type of GreenTeaOption
 * Contains the fields and the def used for the option
 */
protected[dima] trait GreenTeaOption extends GreenTeaObject

/**
 * A GreenTeaCommand holds an optionable of option of type T and defines the options that can still be plugged into it
 *
 * GreenTeaCommand is separated to the optionalbe in order to manipulate independently the AvailableOptions
 *
 * @tparam T the option type of the optionnable command
 * @tparam AvailableOptions the options that can still be plugged
 */
protected[dima] trait GreenTeaCommand[T <: GreenTeaOption, AvailableOptions <: T] extends GreenTeaObject {

  type BodyType <: Optionable {type OptionType = T}

  val body: BodyType

  type BuildType[Available2 <: T] <: GreenTeaCommand[T, Available2]

  def build[Available2 <: T]: BuildType[Available2]

  def plugin[OptionToConsume <: T](o: OptionToConsume) = body.plug(o)
  /* exemple ---> def build[Available2 <: T]: BuildType[Available2] = {
    val b = body
    new GreenTeaCommand[T,Available2] {
      type BodyType = b.BodyType
      val body = b
    }
  }*/
}

/**
 * This class is the mother of the DSL implicit classes used to automatically allow option plug-in
 *
 * Extends an option by allowing the encapsulation of this option into a command and return a new builder
 * that can not take this option anymore
 * * CommandBuilder can not be fused with GreenTeaOption since GreenTeaOption needs to be a trait and CommandBuilder an implicit class
 *
 * CommandsBuilder extends the option and define the calling function
 * This calling function do the required action for configuring the option and evoke the consume method
 *
 * The consume method is defined by the Consumer trait
 */
protected[dima] trait CommandBuilder[T <: GreenTeaOption, AOs <: OptionToConsume, OptionToConsume <: T]
  extends GreenTeaOption with DefaultConsumer[AOs, OptionToConsume] {

  // type BuildType[T] =  command.type#BuildType[T]
  val command: GreenTeaCommand[T, AOs]

  def plug(o: OptionToConsume) = command.plugin(o)

  def consume: command.BuildType[RemainingOptions] = command.build[RemainingOptions]

  def consumeAll: command.BuildType[T] = command.build[T]

  def resetTo[NewOptions <: T]: command.BuildType[NewOptions] = command.build[NewOptions]
}

/**
 * Consumer allow the defintion of Remaining type that is equal to initial type less toConsume type
 * @tparam Initial
 * @tparam ToConsume
 */
protected[dima] trait Consumer[Initial <: ToConsume, ToConsume] {
  type RemainingOptions
}

/**
 * Does not consume anything : syntax coherence up to the develloper (will fail at runtime)
 * @tparam Initial
 * @tparam ToConsume
 */
protected[dima] trait NonConsumingConsumer[Initial <: ToConsume, ToConsume] extends Consumer[Initial, ToConsume] {

  type RemainingOptions = Initial

}

/**
 * Use scala's macro to define remaining (experimental)
 * @tparam Initial
 * @tparam ToConsume
 * http://www.warski.org/blog/2012/12/starting-with-scala-macros-a-short-tutorial/
 * https://weblogs.java.net/blog/cayhorstmann/archive/2013/01/14/first-look-scala-macros
 *
 */
protected[dima] trait MacroConsumer[Initial <: ToConsume, ToConsume] extends Consumer[Initial, ToConsume] {


  import reflect.macros.Context

  type RemainingOptions = Nothing /*   macro computeRemaining */

  def computeRemaining(c: Context): c.Tree = {
    import c.universe._
    /*
val init = typeTag[Initial].tpe
val consumed = typeTag[ToConsume].tpe.typeSymbol

val remain = init.baseClasses.map(t => !t.eq(consumed))

reify(remain).tree */
    reify {}.tree
  }

}

/*
http://engineering.foursquare.com/2011/01/31/going-rogue-part-2-phantom-types/
http://stackoverflow.com/questions/6358651/marking-primitive-types-with-phantom-types-in-scala
http://james-iry.blogspot.ru/2010/10/phantom-types-in-haskell-and-scala.html
http://stackoverflow.com/questions/17882998/type-safe-method-chaining-that-doesnt-allow-repeats-of-operations
   http://dzone.com/snippets/scala-builder-pattern-abstract
   http://blog.rafaelferreira.net/2008/07/type-safe-builder-pattern-in-scala.html#n1



trait TTrue
trait TFalse

@annotation.implicitNotFound(msg = "Cannot call same method twice.")
sealed abstract class =^=[From, To]
object =^= {
 private val singleton_=^= = new =^=[Any, Any]{}
 implicit def tpEquals[A]: A =^= A = singleton_=^=.asInstanceOf[A =^= A]
}

class Myclass[TFoo, TBar, TBuz] private(){
 def foo(implicit e: TFoo =^= TFalse) = new Myclass[TTrue, TBar, TBuz]
 def bar(implicit e: TBar =^= TFalse) = new Myclass[TFoo, TTrue, TBuz]
 def buz(implicit e: TBuz =^= TFalse) = new Myclass[TFoo, TBar, TTrue]
}

object Myclass{
 def apply() = new Myclass[TFalse, TFalse, TFalse]
}

object test {
 Myclass().foo.bar.buz

 Myclass().bar.buz.foo

 Myclass().foo.buz.foo
}

object BuilderPattern {
 sealed abstract class Preparation
 case object Neat extends Preparation
 case object OnTheRocks extends Preparation
 case object WithWater extends Preparation

 sealed abstract class Glass
 case object Short extends Glass
 case object Tall extends Glass
 case object Tulip extends Glass

 case class OrderOfScotch private[BuilderPattern] (val brand:String, val mode:Preparation, val isDouble:Boolean, val glass:Option[Glass])

 abstract class TRUE
 abstract class FALSE

 abstract class ScotchBuilder { self:ScotchBuilder =>
   protected[BuilderPattern] val theBrand:Option[String]
   protected[BuilderPattern] val theMode:Option[Preparation]
   protected[BuilderPattern] val theDoubleStatus:Option[Boolean]
   protected[BuilderPattern] val theGlass:Option[Glass]

   type HAS_BRAND
   type HAS_MODE
   type HAS_DOUBLE_STATUS

   def withBrand(b:String) = new ScotchBuilder {
     protected[BuilderPattern] val theBrand:Option[String] = Some(b)
     protected[BuilderPattern] val theMode:Option[Preparation] = self.theMode
     protected[BuilderPattern] val theDoubleStatus:Option[Boolean] = self.theDoubleStatus
     protected[BuilderPattern] val theGlass:Option[Glass] = self.theGlass

     type HAS_BRAND = TRUE
     type HAS_MODE = self.HAS_MODE
     type HAS_DOUBLE_STATUS = self.HAS_DOUBLE_STATUS
   }

   def withMode(p:Preparation) = new ScotchBuilder {
     protected[BuilderPattern] val theBrand:Option[String] = self.theBrand
     protected[BuilderPattern] val theMode:Option[Preparation] = Some(p)
     protected[BuilderPattern] val theDoubleStatus:Option[Boolean] = self.theDoubleStatus
     protected[BuilderPattern] val theGlass:Option[Glass] = self.theGlass

     type HAS_BRAND = self.HAS_BRAND
     type HAS_MODE = TRUE
     type HAS_DOUBLE_STATUS = self.HAS_DOUBLE_STATUS
   }


   def isDouble(b:Boolean) = new ScotchBuilder {
     protected[BuilderPattern] val theBrand:Option[String] = self.theBrand
     protected[BuilderPattern] val theMode:Option[Preparation] = self.theMode
     protected[BuilderPattern] val theDoubleStatus:Option[Boolean] = Some(b)
     protected[BuilderPattern] val theGlass:Option[Glass] = self.theGlass

     type HAS_BRAND = self.HAS_BRAND
     type HAS_MODE = self.HAS_MODE
     type HAS_DOUBLE_STATUS = TRUE
   }

   def withGlass(g:Glass) = new ScotchBuilder {
     protected[BuilderPattern] val theBrand:Option[String] = self.theBrand
     protected[BuilderPattern] val theMode:Option[Preparation] = self.theMode
     protected[BuilderPattern] val theDoubleStatus:Option[Boolean] = self.theDoubleStatus
     protected[BuilderPattern] val theGlass:Option[Glass] = Some(g)

     type HAS_BRAND = self.HAS_BRAND
     type HAS_MODE = self.HAS_MODE
     type HAS_DOUBLE_STATUS = self.HAS_DOUBLE_STATUS
   }

 }

 type CompleteBuilder = ScotchBuilder {
   type HAS_BRAND = TRUE
   type HAS_MODE = TRUE
   type HAS_DOUBLE_STATUS = TRUE
 }

 implicit def enableBuild(builder:CompleteBuilder) = new {
   def build() =
     new OrderOfScotch(builder.theBrand.get, builder.theMode.get, builder.theDoubleStatus.get, builder.theGlass);
 }

 def builder = new ScotchBuilder {
   protected[BuilderPattern] val theBrand:Option[String] = None
   protected[BuilderPattern] val theMode:Option[Preparation] = None
   protected[BuilderPattern] val theDoubleStatus:Option[Boolean] = None
   protected[BuilderPattern] val theGlass:Option[Glass] = None

   type HAS_BRAND = FALSE
   type HAS_MODE = FALSE
   type HAS_DOUBLE_STATUS = FALSE
 }
}
*/
/////////////////////////////////// //////////////////////////////////////////////////////////////////////
/////////////////////////////////// //////////////////////////////////////////////////////////////////////
/////////////////////////////////// //////////////////////////////////////////////////////////////////////
/////////////////////////////////// Tutorial Example CODE TO IMPLEMENTS Commands and Option :
/////////////////////////////////// //////////////////////////////////////////////////////////////////////
/////////////////////////////////// //////////////////////////////////////////////////////////////////////
/////////////////////////////////// //////////////////////////////////////////////////////////////////////

package gtcommandsTutorial {

/* GeneralOptionType */

trait MySuperOptionType extends GreenTeaOption

object myPrettyCommandOptions {

  type myPrettyOptions = MySuperOptionType with MyOption1 with MyOption2
  type myPrettyOptionsBis = MySuperOptionType with MyOption1 with MyOption2 with MyOption3

  /* Option 1 */
  trait MyOption1 extends MySuperOptionType {

    //fields needed for the option

    //defs needed  for the option

  }

  implicit class MyOption1Builder[AvailableOptions <: MyOption1](val command: GreenTeaCommand[MySuperOptionType, AvailableOptions])
    extends CommandBuilder[MySuperOptionType, AvailableOptions, MyOption1] {
    def option1(args: String) = {
      /* insert here stuff for configuring the option with the parameters */
      consume
    }
  }

  /* Option 2 */
  trait MyOption2 extends MySuperOptionType

  implicit class MyOption2Builder[AvailableOptions <: MyOption2](val command: GreenTeaCommand[MySuperOptionType, AvailableOptions])
    extends CommandBuilder[MySuperOptionType, AvailableOptions, MyOption2] {
    def option2(args: String) = consume
  }

  /* Option 3*/
  trait MyOption3 extends MySuperOptionType

  implicit class MyOption3Builder[AvailableOptions <: MyOption3](val command: GreenTeaCommand[MySuperOptionType, AvailableOptions])
    extends CommandBuilder[MySuperOptionType, AvailableOptions, MyOption3] {
    def option3(args: String) = consume
  }

}

//////////////////////////////////    //////////////////////////////////
// //////////////////////// Commands using those option
// ///////////////////////////////   //////////////////////////////////

import myPrettyCommandOptions._

class myPrettyCommandOptionanable extends Optionable {
  def plug(o: MySuperOptionType) = {}

  type OptionType = MySuperOptionType
}


class myMotherPrettyCommand[PrettyOptions <: MySuperOptionType] extends GreenTeaCommand[MySuperOptionType, PrettyOptions] {
  type BodyType = myPrettyCommandOptionanable
  val body: myPrettyCommand#BodyType = new myPrettyCommandOptionanable
  type BuildType[Available2 <: MySuperOptionType] = myMotherPrettyCommand[Available2]

  def build[Available2 <: MySuperOptionType]: BuildType[Available2] = {
    type B = BodyType
    val b = body
    new myMotherPrettyCommand[Available2] {
      override type BodyType = B
      override val body = b
    }
  }

  def plugin(o: MySuperOptionType): myMotherPrettyCommand[PrettyOptions] = {
    body.plug(o)
    this
  }
}

case class myPrettyCommand() extends myMotherPrettyCommand[myPrettyOptions]

case class myPrettyCommandBis() extends myMotherPrettyCommand[myPrettyOptionsBis]

//////////////////////////////////    //////////////////////////////////
// //////////////////////// Usage
// ///////////////////////////////   //////////////////////////////////

object myPrettyCommandTest {


  myPrettyCommandBis() option1 "ho" option2 "yo" option3 "yo"
  myPrettyCommand() option1 "ho" option1 "yo" //faux  car option 1 redondant
  // myPrettyCommand() option1 "ho" option2 "yo" option3 "yo" //faux car option 3 non pluggable
}

}


/*

*/


/*

val simpleCommand = new GreenTeaCommand[MyOption1, EmptyOption] {
type BodyType = Null
val body: myPrettyCommand#BodyType = null
}

val simpleCommand2 = new GreenTeaCommand[MySuperOptionType, EmptyOption] {
type BodyType = Null
val body: myPrettyCommand#BodyType = null
}

val simpleCommand3 = new GreenTeaCommand[MySuperOptionType, MyOption2] {
type BodyType = Null
val body: myPrettyCommand#BodyType = null
}

//  val yo = new MyOption1Builder[EmptyOption](simpleCommand)

val yo2 = new MyOption1Builder[EmptyOption](simpleCommand2)

val yo3 = new MyOption1Builder[MyOption2](simpleCommand3)


val yoFaux = new MyOption2Builder[MyOption2](simpleCommand3)

// yo.option1("yeah")

yo2.option1("yeah")

yo3.option1("yeah")

//  simpleCommand.option1("youhou")

simpleCommand2.option1("youhou")

simpleCommand3.option1("youhou")

myPrettyCommand().option1("ho")



*/


/**
 * A GreenTeaCommand can be updated by plugin options.
 * The options are plugged into the body (Optionable) of the command
 *
 *
 * tparam T : This optionable object can only be plugged with option of type T
 * tparam AvailableOption the set of commands that can be plugged
 * type BodyType : the type of command object

protected[dima] trait GreenTeaCommand[+T <: GreenTeaOption, AvailableOption <: GreenTeaOption] extends GreenTeaObject {

  type BodyType <: Optionable[T]

  val body: BodyType

  protected[language] def plug[OptionToPlug <: GreenTeaOption](opt: OptionToPlug) = {
    body.plug(opt)
    val b = this.body
    new GreenTeaCommand[PluggableOption, PluggedOptions with OptionToPlug] {
      val body: BodyType = b
      type BodyType = b.type
    }
  }

}
 */


/*

/**
*
protected[dima] trait GreenTeaOption extends GreenTeaObject with Ordered[GreenTeaOption] {

val precedence = 0

def compare(that: GreenTeaOption): Int = this.precedence compare that.precedence

}
*/



  */

/*
class PerformativeCommand[PluggedOption <: GreenTeaOption, +I <: Identifier](val key: String = "")
extends Message with GreenTeaCommand[PerformativeOption, PluggedOption] {

def apply() = {
  body.send(this)
}

}
*/


/*
protected[language] def cloneCommand[RemainingOptions <: GreenTeaOption]: GreenTeaCommand[RemainingOptions] = {
  val p = new ProactivityCommand[RemainingOptions] {
    ActivityPa
  }
  p.options = options
  p.action = action.asInstanceOf[p.ActivityAction]
  p.userPrecedence = userPrecedence
  return p
} */

/*override def updateOption[RemainingOption <: AvailableOptions, ConsumedOption <: AvailableOptions]
(opt: ConsumedOption): PerformativeCommand[RemainingOption, I] = {
val p = new PerformativeCommand[RemainingOption, I] {
 type OptionType = this.OptionType
}
p.options = options
return p
}      */


//type Options <: GreenTeaOption  //etend une liste d'un type
//var options: Option[Options] = None
//var reactionId: Option[GreenTeaLeaf => Proactivity]


//protected[dima] class GreenTeaOptionBush[OptionType <: GreenTeaOption] extends OptionType with mutable.HashSet[OptionType]


//protected[dima] abstract class Preactivity(implicit component: ComponentIdentifier) extends Proactivity
/**
 * trick used by the commands
protected[dima] object proactivity extends Proactivity{
  type ReturnType =  ExecutionStatus
  type ActivityParameters = Unit

  def execute    = continue()

  val systemPrecedence: Int = 0
}

 */

/*{
assert(activeMethods contains proact)
activeMethods -= proact
stoppedMethods += proact
}     */

/** ********************
  * PRIMTIVES
  */

//private def executeActivityMethods {
//[P: Activity] {
/* stepMethods.filter(t).foreach({
  m =>
    m() match {
    interrupted (h) => {
    stepMethods.get (preActivity () ) -= m
    assert ! interruptMethods.get (preActivity () ).contains (m)
    interruptMethods.get (preActivity () ) += m
    isFreed += (h, m)
    }
    stopped (h) => stepMethods.get (preActivity () ) -= m
    }
}  */


/**
 * Activity methods' action have optional parameters.
 * This class is used to stock them when constructing the action
 * The parameters are curryfied to be passed to the action
 *
 * param messages the events received for a reactivity method
 * param knowing the result of asynchronous call

protected[dima] class ProactivityParameters(
                                             val messages: Option[List[Message[PerformativeOption]]]
val know: Option[knowledge.Knowledge]
)
extends GreenTeaObject
 */


/*
/*
Those methods allow to indivually control each activity
*/

def interruptActivity[R <: Return](proact: Activity[R], isFreed: clock.Hook) =
  this ~(proact, returns.interrupted(isFreed))

def stopActivity[R <: Return](proact: Activity[R]) = this - proact

def reactivateActivity[R <: Return](proact: Activity[R]) = this + proact
      */


//////////////////////////////////
// //////////////////////// GREENTEA Language  Public Interface
// ///////////////////////////////
/*
trait NoOption extends GreenTeaOption


object NoOption[RemainingOptions <: GreenTeaOption] extends NoOption[RemainingOptions] {

  type ExecutionParameters = Unit

  def getParameters: NoOption.ExecutionParameters = {}

  def isFreed: Boolean = true
}
*/


//////////////////////////////////    //////////////////////////////////
// //////////////////////// GREENTEA Commands
// ///////////////////////////////   //////////////////////////////////
