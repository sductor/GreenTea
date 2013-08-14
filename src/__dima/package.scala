
/**
GreenTea Language
Copyright 2013 Sylvain Ductor
  * */
/**
This file is part of GreenTeaObject.

GreenTeaObject is isFreed software: you can redistribute it and/or modify
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

import dima.dsl._
import dima.dsl.performatives._
import dima.greentea._

import dima.monitors.GreenTeaException
import scala.collection.mutable
import scala.reflect.runtime.universe._
import scala.collection.immutable.HashSet

package dima {


protected[dima] class Ticker(t: Any) extends Hook {
  def isFreed: Boolean = ???
}

sealed class FuzzyTicker

case class veryLow() extends FuzzyTicker

case class low() extends FuzzyTicker

case class normal() extends FuzzyTicker

case class high() extends FuzzyTicker

case class veryHigh() extends FuzzyTicker


//This class is used to denote the return of a asynchronous function execution
trait AsynchronousExecution extends GreenTeaException

object AsynchronousExecution extends AsynchronousExecution

}

package object dima {

  ////////////////////////////////////////////////
  // ////////////////////// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! /* DSL CONFIGURATION */
  ////////////////////////////////////////////////

  type DefaultConsumer[Initial <: ToConsume, ToConsume] = NonConsumingConsumer[Initial, ToConsume]

  /* */

  private type BasicActivityOptions = ActivityOption with ActionOption with WhenOption with KnowledgeOption

  type ProactivityOptions = BasicActivityOptions with FrequenceOption with MailsOption

  type ReactivityOptions = BasicActivityOptions


  /* */

  private type BasicPerformativeOptions = PerformativeOption
    with SendActionOption with ReceiverNinReplyToOption with ReplyToOption
    with ReplyWithOption
    with PropagateOption with ProxyOption

  type ACLOptions = BasicPerformativeOptions

  type LogOptions = BasicPerformativeOptions

  type AcquaintanceOptions = BasicPerformativeOptions

  type ExceptionOptions = BasicPerformativeOptions

  ////////////////////////////////////////////////
  // ////////////////////// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! /* END OF DSL CONFIGURATION */
  ////////////////////////////////////////////////

  ////////////////////////////////////////////////  ////////////////////////////////////////////////
  // ////////////////////// /* Most Basic Definitions */
  ////////////////////////////////////////////////  ////////////////////////////////////////////////

  //Most General Identifier
  type Identifier = CoalitionIdentifier

  /* object Identifier extends SeqFactory[List]{
     type A = Agent
     def newBuilder[A]: mutable.Builder[A, List[A]] = new ListBuffer[A]
   }   */


  ////////////////////////////////////////////////  ////////////////////////////////////////////////
  // ////////////////////// /* DSL IMPLICITS */
  ////////////////////////////////////////////////  ////////////////////////////////////////////////


  trait GreenTeaObject extends Serializable {
    ////////////////////////////////////////////////
    // ////////////////////// /* IDENTIFICATION IMPLICITS */
    ////////////////////////////////////////////////
    /*
* Implicit conversion of String, List[String] et List[Identifier] to Identifier
*/

    implicit def stringToIDentifier(id: String): Identifier =
      new AgentIdentifier(id)

    implicit def privateIDentifierToAgentIdentifier[_](id: PrivateIdentifier[_]): AgentIdentifier =
      new AgentIdentifier(id.name)

    implicit def nameToList(ids: List[String]): Identifier =
      new Identifier(
        name = new AgentIdentifier(ids.head).nextCoalitionName,
        members = HashSet(ids.map(n => new AgentIdentifier(n)))) {
        val proxy = new AgentIdentifier(ids.head)
      }

    //lourd


    implicit def idToList(ids: List[Identifier]): Identifier =
      new Identifier(name = ids.head.nextCoalitionName, members = Set().flatMap(ids)) {
        val proxy = new AgentIdentifier(ids.head.name)
      }


    class Typed[A](value: A)(implicit val key: TypeTag[A]) {
      def toPair: (TypeTag[_], Any) = (key, value)
    }

    // object Typed {
    implicit def toTyped[A: TypeTag](a: A) = new Typed(a)

    implicit def toTypable[A](a: A) = new {
      def typedAs[O >: A : TypeTag] = new Typed[O](a)(typeTag[O])
    }
    // }
    ////////////////////////////////////////////////
    // ////////////////////// /* Option IMPLICITS */
    ////////////////////////////////////////////////

    implicit def boolToFun(w: Boolean): () => Boolean = {
      def whenDef(): Boolean = w
      whenDef
    }

    implicit def intToTicker(t: Int): Ticker = new Ticker(t)

    implicit def fuzzyTickerToTicker(ft: FuzzyTicker): Ticker = new Ticker(ft)

    ////////////////////////////////////////////////
    // ////////////////////// /* RETURN TYPES IMPLICITS */
    ////////////////////////////////////////////////

    implicit def interrupt(hook: Hook): interrupted = {
      val i = interrupted()
      i.h = Some(hook)
      i
    }

    implicit def defaultInitializationReturn: InitializationStatus = notInitialized()

    implicit def defaultExecutionReturn[P](a: P => _): P => ExecutionStatus = a.andThen(_ => continue())

    implicit def defaultTerminationReturn: TerminationStatus = terminated()

    implicit def defaultReactivityReturn: Either[_, GreenTeaException] = Right(AsynchronousExecution)

    ////////////////////////////////////////////////
    // ////////////////////// /* EXCEPTION IMPLICITS */
    ////////////////////////////////////////////////

    implicit def exceptionToGreenTea(ex: Throwable) = new GreenTeaException(Some(ex))

    implicit def greenTeexceptionToOption(e: GreenTeaException): Option[GreenTeaException] = Some(e)

    implicit def exceptionToOptionGreenTea(ex: Throwable) = Some(new GreenTeaException(Some(ex)))
  }


  //////////////////////////////////////////////// ////////////////////////////////////////////////
  // ////////////////////// /* Tools */
  ////////////////////////////////////////////////  ////////////////////////////////////////////////

  trait mutableMap[K, V] extends mutable.Map[K, V] with  GreenTeaObject{

    val localMap = new mutable.HashMap[K, V]()

    def +=(kv: (K, V)): this.type = {
      localMap += kv;
      this
    }

    def -=(key: K): this.type = {
      localMap -= key;
      this
    }

    override def empty: mutable.Map[K, V] = localMap.empty

    def get(key: K): Option[V] = localMap.get(key)

    def iterator: Iterator[(K, V)] = localMap.iterator

  }



  trait TypedMap extends GreenTeaObject{

    // Didier Dupont   http://stackoverflow.com/questions/7335946/class-type-as-key-in-map-in-scala



    private val inner = new mutable.HashMap[TypeTag[_], Any]()

    def +=[A](t: Typed[A]) = {
      inner += t.toPair
    }

    def +=[A: TypeTag](a: A) = {
      inner += (typeTag[A] -> a)
    }

    def -=[A: TypeTag]() = {
      inner -= typeTag[A]
    }
    def forall[A](p: A => Boolean): Boolean = ???
    /* */

    //def apply[A: TypeTag]: A = inner(typeTag[A]).asInstanceOf[A]

    def apply[A: TypeTag]: Option[A] = inner.get(typeTag[A]).map(_.asInstanceOf[A])



    def values = inner.values

    // def apply(items: Typed[_]*) = new TypedMap(Map(items.map(_.toPair): _*))
  }

  /* object TypedTest {

     import Typed._

     val repository = TypedMap("foo", 12, "bar".typedAs[Any])

     repository[String] // returns "foo"
     repository.get[Any] // returns Some("bar")
   }     */
  trait TypedKey  {
    type GET

    protected val get: GET

    def unary_~ : GET = get
  }
}


/*
type Hook = GreenTeaSeed with Identification[Identifier] {

def isFreed: Unit => Boolean

}    */
