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

package bigtop
package core

import java.net.{URL,URLEncoder}
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._

trait UrlParam[T] {
  /** The URL query parameter to interrogate. */
  def paramName: String
  
  /**
  * Convert a meaningful value to a query param.
  * Return Some[String] if we need a query param, or None if we should omit it.
  */
  def encodeParam(value: T): Box[String]
  
  /** Convert the value of a query param to a meaningful value. */
  def decodeParam(str: Box[String]): T

  /** Convert the relevant query param in the URL to a meaningful value. */
  def decodeCurrentUrl: T =
    decodeParam(S.param(paramName))
  
  /** Update the supplied URL, setting or removing the relevant query param as necessary. */
  def rewriteUrl(url: String, value: T): String = 
    rewriteUrl(Url(url), value).toString

  /** Update the supplied URL, setting or removing the relevant query param as necessary. */
  def rewriteUrl(url: Url, value: T): Url =
    encodeParam(value).
    map(url.set(paramName, _)).
    getOrElse(url.remove(paramName))

}

trait UrlVar[T] {
  self: AnyVar[T, _] =>
  
  def param: UrlParam[T]
  
  def rewriteUrl(url: String): String =
    param.rewriteUrl(url, is)

  def rewriteUrl(url: Url): Url =
    param.rewriteUrl(url, is)
  
  def rewriteCurrentUrl: Url =
    rewriteUrl(Url.liftPathAndQuery.open_!)
}

abstract class UrlRequestVar[T](val param: UrlParam[T]) extends RequestVar[T](param.decodeCurrentUrl) with UrlVar[T] {
  override def __nameSalt = net.liftweb.util.Helpers.randomString(10)
}

abstract class TransientUrlRequestVar[T](val param: UrlParam[T]) extends TransientRequestVar[T](param.decodeCurrentUrl) with UrlVar[T] {
  override def __nameSalt = net.liftweb.util.Helpers.randomString(10)
}

// Concrete implementations ---------------------

object UrlParam {
  
  case class StringParam(val paramName: String, default: String) extends UrlParam[String] {
    def encodeParam(value: String): Box[String] = if(value == default) Empty else Full(value)
    def decodeParam(param: Box[String]): String = param.openOr(default)
  }
  
  case class OptionStringParam(val paramName: String) extends UrlParam[Option[String]] {
    def encodeParam(value: Option[String]): Box[String] = value
    def decodeParam(param: Box[String]): Option[String] = param
  }
  
  case class BooleanParam(val paramName: String, default: Boolean) extends UrlParam[Boolean] {
    def encodeParam(value: Boolean): Box[String] = if(value == default) Empty else if(value) Full("yes") else Full("no")
    def decodeParam(param: Box[String]): Boolean = param.map(_ == "yes").getOrElse(default)
  }
  
  case class IntParam(val paramName: String, default: Int) extends UrlParam[Int] {
    def encodeParam(value: Int): Box[String] = if(value == default) Empty else Full(value.toString)
    def decodeParam(param: Box[String]): Int = param.flatMap(s => tryo(Integer.parseInt(s))).openOr(default)
  }
  
  case class OptionIntParam(val paramName: String) extends UrlParam[Option[Int]] {
    def encodeParam(value: Option[Int]): Box[String] = value.map(_.toString)
    def decodeParam(param: Box[String]): Option[Int] = param.flatMap(s => tryo(Integer.parseInt(s)))
  }

}

abstract class StringUrlRequestVar(val paramName: String, default: String) extends UrlRequestVar[String](UrlParam.StringParam(paramName, default))

abstract class OptionStringUrlRequestVar(val paramName: String) extends UrlRequestVar[Option[String]](UrlParam.OptionStringParam(paramName))

abstract class BooleanUrlRequestVar(val paramName: String, default: Boolean) extends UrlRequestVar[Boolean](UrlParam.BooleanParam(paramName, default))

abstract class IntUrlRequestVar(val paramName: String, default: Int) extends UrlRequestVar[Int](UrlParam.IntParam(paramName, default))

abstract class OptionIntUrlRequestVar(val paramName: String) extends UrlRequestVar[Option[Int]](UrlParam.OptionIntParam(paramName))
