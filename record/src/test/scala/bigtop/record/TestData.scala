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
package record

import net.liftweb.record._
import net.liftweb.record.field._

import org.scalatest._

// Test data ------------------------------------
  
class User private() extends Record[User] with BaseUser[User] {
  def meta = User
  
  object id extends IntField(this)
  object name extends StringField(this, 128)
  
  def save: User = this
  def delete: User = this

  def usernameTaken(username: String): Boolean =
    false
}

object User extends User with MetaRecord[User] with BaseUserMeta[User] {
  
  def byUsername(username: String): Option[User] =
    if(username == "invalid") None else Some(createRecord.username(username))

  def byEmail(email: String): Option[User] =
    if(email == "invalid") None else Some(createRecord.email(email))
  
  def byEmailVerificationCode(code: String): Option[User] =
    if(code == "invalid") None else Some(createRecord.emailVerificationCode(Some(code)))
  
}
