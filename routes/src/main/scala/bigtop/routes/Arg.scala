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
package routes

// The Pattern encodes requires we specify if an Arg is a
// Literal or a Match. Hence we seal the Arg trait and
// require implementations to use the corresponding
// sub-traits.

sealed trait Arg[T] {

  def encode(value: T): String
  def decode(path: String): Option[T]
  
  def urlEncode(str: String): String =
    java.net.URLEncoder.encode(str, "utf-8")
  
  def urlDecode(str: String): String =
    java.net.URLDecoder.decode(str, "utf-8")
  
}

trait LiteralArg extends Arg[Unit]

trait MatchArg[T] extends Arg[T]

object IntArg extends MatchArg[Int] {
  
  def encode(value: Int) =
    urlEncode(value.toString)
  
  def decode(path: String) =
    try {
      Some(urlDecode(path).toInt)
    } catch {
      case exn: NumberFormatException => None
    }
  
}

object StringArg extends MatchArg[String] {
  
  def encode(value: String) =
    urlEncode(value)
  
  def decode(path: String) =
    Some(urlDecode(path))
  
}

case class ConstArg(value: String) extends LiteralArg {

  def encode(v: Unit): String = value
  
  def decode(path: String) = {
    if (path == value)
      Some(path)
    else
      None
  }
}

trait ArgOps {
  implicit def stringToArg(v: String): LiteralArg = ConstArg(v)
}

object ArgOps extends ArgOps
