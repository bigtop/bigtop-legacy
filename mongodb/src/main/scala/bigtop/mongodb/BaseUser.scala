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
package mongodb

import scala.xml._

import net.liftweb.common._
import net.liftweb.http.{SessionVar, RequestVar}
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.util._

import com.foursquare.rogue._
import com.foursquare.rogue.Rogue._

trait BaseUser[T <: BaseUser[T]] extends IdRecord[T] with bigtop.record.BaseUser[T] {
  self: T =>

  val firstName = new StringField(this, 256) {
    override def setFilter = (trim _) :: super.setFilter
    override def validations =
      valMinLen(1, "Please enter your first name") _ ::
      super.validations
  }
  
  val lastName = new StringField(this, 256) {
    override def setFilter = (trim _) :: super.setFilter
    override def validations =
      valMinLen(1, "Please enter your last name") _ ::
      super.validations
  }
  
  def fullName = firstName.is + " " + lastName.is
  
  def usernameTaken(username: String): Boolean =
    baseUserMeta.byUsername(username) map {
      that => that.username.is == username && that.id != this.id
    } getOrElse false

}

trait BaseUserMeta[T <: BaseUser[T]] extends IdRecordMeta[T] with bigtop.record.BaseUserMeta[T] {
  self: T =>
  
  def byUsername(username: String): Option[T] =
    this.where(_.username eqs username).get
  
  def byEmail(email: String): Option[T] =
    if(email.trim == "") None else this.where(_.email eqs email).get

  def byEmailVerificationCode(code: String): Option[T] =
    if(code.trim == "") None else this.where(_.emailVerificationCode eqs code).get

}
