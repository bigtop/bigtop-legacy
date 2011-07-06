/* 
 * Copyright 2011 Untyped Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bigtop.debug

import scala.text._
import net.liftweb.common.{Logger, Loggable}

/**
 * Trait providing debug() methods that accept an argument,
 * log it somewhere, and return it.
 */
trait Debug {
  
  lazy val debugName = getClass.getSimpleName
  
  def debug[T](fn: => T): T = debug(None, fn)
  
  def debug[T](message: String, fn: => T): T = debug(Some(message), fn)
  
  def debug[T](message: Option[String], fn: => T): T = {
    val ans = fn
    debugInternal(message, ans)
    ans
  }
  
  def debugInternal[T](message: Option[String], value: T): Unit =
    debugPrint(debugFormatAll(message, value))
  
  def debugFormatAll[T](message: Option[String], value: T): Document =
    DocText(message.getOrElse(debugName) + ":") :/:
    DocNest(2, debugFormat(value)) :/:
    DocNil

  def debugFormat[T](value: T): Document =
    DocText(value.toString)

  def debugPrint(all: Document): Unit
  
  def documentToString(doc: Document): String = {
    val out = new java.io.StringWriter
    doc.format(80, out)
    out.close
    out.toString
  }

}

// Console --------------------------------------

/** Send debug messages to the good old-fashioned console. */
trait ConsoleDebug extends Debug {
  def debugPrint(doc: Document): Unit = println(documentToString(doc))
}

/** Default console logger. */
object ConsoleDebug extends ConsoleDebug

// Loggable -------------------------------------

/** Debug messages that are compatible with Lift's Loggable mixin. */
trait LoggableDebug extends Debug with Loggable {
  def debugPrint(doc: Document): Unit = logger.debug(documentToString(doc))
}

/** Send debug messages to a Lift Logger object. */
class LoggerDebug(val logger: Logger) extends Debug {
  def debugPrint(doc: Document): Unit = logger.debug(documentToString(doc))
  
  def apply[T](fn: => T): T = debug(fn)
  def apply[T](message: String, fn: => T): T = debug(message, fn)
  def apply[T](message: Option[String], fn: => T): T = debug(message, fn)
}
