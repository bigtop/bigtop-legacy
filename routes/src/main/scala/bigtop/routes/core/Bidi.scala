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

/**
 * Bidirectional mapping between two types, Inner and Outer, via means of a pair of methods:
 *
 *  - the decode() method converts from an Outer value to an Inner value;
 *  - the encode() method converts from an Inner value to an Outer value.
 *
 * Inner values are assumed to be well typed, whereas Outer values are considered to come from 
 * the outside world. It is assumed that the decode() operation is unreliable and may fail. decode()
 * therefore returns an Option[Inner], whereas encode() returns an unwrapped Outer.
 * 
 * An extra method, canDecode(), can be used to perform a quick syntax check on the Outer value in
 * situations where a full decode() is computationally expensive (e.g. where a database lookup is
 * required).
 */
trait Bidi {
  
  type Outer
  type Inner

  /**
   * Perform a quick check to indicate whether decode() is likely to succeed.
   *
   * Override this to provide a guard in situations where decode() is computationally expensive.
   */
  def canDecode(a: Outer): Boolean =
    decode(a).isDefined

  /**
   * Convert an Outer to an Inner. It is assumed that the conversion is unreliable and may fail. 
   * The result is therefore an Option[Inner], where None indicates failure.
   */
  def decode(a: Outer): Option[Inner]
  
  /** Convert an Inner to an Outer. */
  def encode(b: Inner): Outer

}
