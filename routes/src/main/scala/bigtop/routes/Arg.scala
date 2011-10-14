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

/**
 * Trait describing the required behaviour to map URL path segments
 * to typed values we can use in our applications.
 *
 * For example, the URL:
 * 
 *   http://example.com/abc/123
 *
 * has a path "/abc/123" containing two segments: "abc" and "123".
 *
 * You can subclass this for any T: String, Int, Person, etc.
 */
trait Arg[T] {

  /**
   * Attempt to decode a URL path segment into a typed value.
   * Return Some(value) if successful, None if unsuccessful.
   */
  def decode(in: String): Option[T]
  
  /** Encode a typed value as a URL path segment. */
  def encode(in: T): String

}

/** Maps between path segments and integers. */
case object IntArg extends Arg[Int] {
  
  def encode(in: Int) =
    in.toString
  
  def decode(in: String) =
    try {
      Some(in.toInt)
    } catch {
      case exn: NumberFormatException => None
    }
  
}

/** Maps between path segments and strings. Escapes/unescapes characters that are reserved in URLs. */
case object StringArg extends Arg[String] {
  
  def encode(in: String) =
    in
  
  def decode(in: String) =
    Some(in)
  
}
