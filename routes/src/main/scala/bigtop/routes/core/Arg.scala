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

package bigtop.routes.core

/** Bidirectional map between a segment of a URL path and a typed Scala value. */
trait Arg[X] extends Bidi {
  
  type Outer = String
  type Inner = X
  
  /**
   * Perform a quick check to see whether a url can be decoded by decode().
   * The default implementation provided here simply returns true.
   *
   * Override this to provide a quick sanity check in situations where decode() performs
   * an expensive computation (e.g. performing a database lookup).
   *
   * The default implementation calls decode() and checks whether the result is an instance of Some.
   */
  override def canDecode(a: Outer): Boolean =
    decode(a).isDefined
  
  /**
   * Converts a path segment to a typed value.
   * 
   * Assumes that the path segment has already been unescaped ("%2F" converted to "/" and so on).
   */
  def decode(a: Outer): Option[Inner]
  
  /**
   * Converts a typed Scala value to a URL path segment.
   * 
   * Leaves the path segment unescaped (i.e. does not convert "/" to "%2F" and so on).
   */
  def encode(b: Inner): Outer

}

/** Arg for mapping URL path segments to/from integer values. */
case object IntArg extends Arg[Int] {
  
  /** Attempt to convert a path segment as an Int. */
  def decode(in: String) =
    try {
      Some(in.toInt)
    } catch {
      case exn: NumberFormatException => None
    }

  /** Convert an Int to a path segment. */
  def encode(in: Int) =
    in.toString
  
}

/** Arg for mapping URL path segments to/from double values. */
case object DoubleArg extends Arg[Double] {
  
  /** Attempt to convert a path segment as an Double. */
  def decode(in: String) =
    try {
      Some(in.toDouble)
    } catch {
      case exn: NumberFormatException => None
    }

  /** Convert a Double to a path segment. */
  def encode(in: Double) =
    in.toString
  
}

/** Arg for mapping URL path segments to/from Java strings. */
case object StringArg extends Arg[String] {
  
  /** Convert a path segment to a String (this is actually an identity function). */
  def decode(in: String) =
    Some(in)
  
  /** Convert a String to a path segment (this is actually an identity function). */
  def encode(in: String) =
    in
  
}
