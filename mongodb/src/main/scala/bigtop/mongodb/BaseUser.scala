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

class PasswordProxy[R <: Record[R]](passwordHash: StringField[R], passwordSalt: StringField[R]) {

  def is: String = ""
  
  def apply(pass: String): R = {
    val salt = Helpers.hash(Helpers.nextFuncName)
    val hash = SecurityHelpers.hash(pass + salt)
    
    passwordHash(hash)
    passwordSalt(salt)
  }
  
  def match_?(pass: String): Boolean =
    passwordHash.is == SecurityHelpers.hash(pass + passwordSalt.is)

}

trait BaseUser[T <: BaseUser[T]] extends IdRecord[T] {
  self: T =>

  val username = new StringField(this, 256) {
    override def setFilter = (trim _) :: super.setFilter
    override def validations =
      valMinLen(1, "Please enter a username") _ ::
      super.validations
  }

  // SHA encrypted password:
  val passwordHash = new StringField(this, 28) with IgnoreInEquals
  val passwordSalt = new StringField(this, 28) with IgnoreInEquals
  
  // TODO: Replace User.password with a regular PasswordField when it is fixed (Lift bug #937):
  val password = new PasswordProxy(passwordHash, passwordSalt)
  
  val email = new EmailField(this, 256) {
    override def setFilter = (trim _) :: super.setFilter
    override def validations =
      valMinLen(1, "Please enter an email address") _ ::
      super.validations
  }

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
  
  val superUser = new BooleanField(this)
  
  val identityVerified = new BooleanField(this)
  val identityVerificationCode = new StringField(this, 28)
  
  def fullName =
    firstName.is + " " + lastName.is
  
  def canLogIn = identityVerified.is
  
}

trait BaseUserMeta[T <: BaseUser[T]] extends IdRecordMeta[T] {
  self: T =>
  
  // Real identity ------------------------------
  
  private object realUsernameVar extends SessionVar[Box[String]](Empty)
  private object realUserVar extends RequestVar[Box[T]](
    realUsernameVar.is.flatMap(byUsername(_).get))

  def realUsername = realUsernameVar.is
  def realUser = realUserVar.is
  
  // Effective identity -------------------------
  
  private object effectiveUsernameVar extends SessionVar[Box[String]](Empty)
  private object effectiveUserVar extends RequestVar[Box[T]](
    effectiveUsernameVar.is.flatMap(byUsername(_).get))

  def effectiveUsername = effectiveUsernameVar.is
  def effectiveUser = effectiveUserVar.is
    
  // User queries -------------------------------
  
  def byUsername(username: String): Query[T] =
    this.where(_.username eqs username)
  
  def byUsernameAndPassword(username: String, password: String): Option[T] =
    byUsername(username).get.filter(_.password.match_?(password))

  // Password validation ------------------------
  
  def isPasswordValid(password: String) =
    password.length >= 6
  
  val invalidPasswordMessage =
    Text("Please enter six characters or more")

  // Log in, log out ----------------------------
  
  def isLoggedIn = effectiveUsername.isDefined
  
  def logIn(username: String, password: String): Boolean =
    byUsernameAndPassword(username, password).
    headOption.
    filter(user => user.canLogIn).
    map(user => { logIn(user); true }).
    getOrElse(false)
  
  def logIn(user: T): Unit =
    logIn(user.username.is)
  
  def logIn(username: String): Unit = {
    realUserVar.remove
    effectiveUserVar.remove
    
    realUsernameVar.set(Full(username))
    effectiveUsernameVar.set(Full(username))
    
    onLogIn.foreach(_(effectiveUser))
  }
   
  def logOut: Unit = {
    val real = realUser
    val effective = effectiveUser
    
    realUsernameVar.remove
    realUserVar.remove
    effectiveUsernameVar.remove
    effectiveUserVar.remove
    
    onLogOut.foreach(_())
  }

  def onLogIn: List[(Box[T]) => Unit] = Nil
  def onLogOut: List[() => Unit] = Nil
  
  // Change identity, restore identity ----------
  
  def changeIdentity(user: T): Unit =
    changeIdentity(user.username.is)
  
  def changeIdentity(username: String): Unit = {
    effectiveUserVar.remove
    effectiveUsernameVar.set(Full(username))
    
    onChangeIdentity.foreach(_(effectiveUser))
  }
   
  def restoreIdentity: Unit = {
    effectiveUserVar.remove
    effectiveUsernameVar.set(realUsernameVar.get)
    
    onRestoreIdentity.foreach(_())
  }

  def onChangeIdentity: List[(Box[T]) => Unit] = Nil
  def onRestoreIdentity: List[() => Unit] = Nil
  
}
