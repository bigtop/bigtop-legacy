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

import org.mindrot.jbcrypt.{BCrypt => JBCrypt}
import net.liftweb.util.Props

/** Helpers for using jBCrypt */
object BCrypt {

  /** Length of a BCrypt hashed password, in characters */
  val hashedLength = 60

  /**
   * (Log) number of founds to use when creating salt. Defaults to the value of
   * the bigtop.record.rounds property, or 10 if that is not set
   */
  lazy val rounds = Props.getInt("bigtop.record.rounds", 10)

  /** Compare a plain text password to hashed one, returning true if they are equal */
  def compare(plainText: String, hash: String): Boolean = JBCrypt.checkpw(plainText, hash)

  /** Hash a plain text password, using the value of rounds to generate salt */
  def hash(plainText: String) = JBCrypt.hashpw(plainText, JBCrypt.gensalt(rounds))

  /** Hash a plain text password, given salt */
  def hash(plainText: String, salt: String) = JBCrypt.hashpw(plainText, salt)

  /** Create salt using the value of rounds */
  def createSalt() = JBCrypt.gensalt(rounds)

  /** Create salt using the given number of rounds */
  def createSalt(rounds: Int) = JBCrypt.gensalt(rounds)

  /** True if the string looks like a BCrypt hash */
  def isHash(str: String): Boolean = 
    str.length == hashedLength &&
    HashRegex.findFirstIn(str).isDefined
  
  // Internal stuff --------------------------------------------------

  val HashRegex = "^\\$a2\\$[0-9]{2}\\$".r
  
}
