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

import scala.xml._

import net.liftweb.common._
import net.liftweb.http.{S,SHtml,RequestVar,SessionVar}
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.util.Mailer._

/**
 * Trait that all concrete BaseUser implementations must implement to
 * work with this package
 */
trait BaseUser[T <: BaseUser[T]] extends Record[T] with Loggable {
  self: T =>
  
  protected def baseUserMeta =
    meta.asInstanceOf[BaseUserMeta[T]]
  
  def save: T
  
  def delete: T
  
  val username = new StringField(this, 256) {
    override def setFilter =
      trim _ ::
      super.setFilter
    
    override def validations =
      valMinLen(1, "Please enter a username") _ ::
      valUnique("That username is already taken") _ ::
      super.validations
    
    def valUnique(msg: String)(username: String): List[FieldError] =
      if(baseUserMeta.byUsername(username) == self) Nil else List(FieldError(this, msg))
  }

  // bcrypt encrypted password:
  val password = new bigtop.record.PasswordField(this) {
    override def validations =
      valPasswordValid _ ::
      super.validations
    
    def valPasswordValid(str: String): List[FieldError] =
      if(baseUserMeta.isValidPassword(str)) {
        Nil
      } else {
        List(FieldError(this, baseUserMeta.invalidPasswordMessage))
      }
  }
  
  val email = new EmailField(this, 256) {
    override def setFilter =
      trim _ ::
      super.setFilter
    
    override def validations =
      valMinLen(1, "Please enter an email address") _ ::
      super.validations
  }
  
  /**
   * Unique code used to verify the user's email address.
   * 
   * A new code is generated when the user signs up or clicks "forgot password".
   * The code is placed into an email verification URL, which is then hooked up
   * to a piece of code that sets user.emailVerified to true and clears the code.
   *
   * BaseUser does not hook up email verification URLs automatically.
   * The programmer must implement these using the (simple) snippets provided.
   */
  val emailVerificationCode = new OptionalStringField(this, 28)
  
  /**
   * Has the user's email address been verified?
   * See emailVerificationCode for more information.
   */
  val emailVerified = new BooleanField(this)

  /**
   * Is this user allowed to log in?
   * 
   * The default implementation returns true if the user's email address 
   * has been verified. See emailVerificationCode for more information.
   */
  def canLogIn = emailVerified.is
  
  /** Is this user allowed to change identity? False by default. */
  def canChangeIdentity: Boolean = false
  
  def makeEmailVerificationCode: String =
    Helpers.nextFuncName
  
  def sendSignUpEmail = {
    val code = makeEmailVerificationCode
    
    emailVerificationCode(Some(code)).emailVerified(false).save
    
    sendEmail(
      "signup email",
      signUpEmailFrom,
      signUpEmailSubject,
      signUpEmailTo,
      signUpEmailBody(signUpEmailUrl(code)))
  }
  
  def signUpEmailFrom: From = From("noreply@example.com")
  def signUpEmailTo: To = To(email.is)
  def signUpEmailSubject: Subject = Subject("Welcome")
  def signUpEmailUrl(code: String) = "/reset-password?code=" + code
  
  def signUpEmailBody(url: String): MailBodyType =
    XHTMLMailBodyType(
      <body>
        <p>Please click on the link below to activate your account:</p>
      
        <p><strong>&nbsp;&nbsp;<a href={url}>{url}</a></strong></p>
      
        <p>Once you have done this you will be able to log in.</p>
      
        <p style="font-size: .9em">(Please do not reply to this message.)</p>
      </body>)

  def sendForgotPasswordEmail = {
    val code = makeEmailVerificationCode
    
    emailVerificationCode(Some(code)).save
    sendEmail(
      "forgot password",
      forgotPasswordEmailFrom,
      forgotPasswordEmailSubject,
      forgotPasswordEmailTo,
      forgotPasswordEmailBody(forgotPasswordEmailUrl(code)))
  }
  
  def forgotPasswordEmailFrom: From = From("noreply@example.com")
  def forgotPasswordEmailTo: To = To(email.is)
  def forgotPasswordEmailSubject: Subject = Subject("Forgot password")
  def forgotPasswordEmailUrl(code: String) = "/reset-password?code=" + code
  
  def forgotPasswordEmailBody(url: String): MailBodyType =
    XHTMLMailBodyType(
      <body>
        <p>Please click on the link below to reset your password:</p>
        
        <p><a href={url}>{url}</a></p>
        
        <p>Please disregard this message if you believe you have been sent it incorrectly.</p>
        
        <p style="font-size: .9em">(Please do not reply to this message.)</p>
      </body>)
  
  def sendEmail(desc: String, from: From, subject: Subject, to: To, body: MailBodyType) = {
    logger.info("Sending " + desc + " email:\n" + from + "\n" + subject +  "\n" + to + "\n" + body)
    Mailer.sendMail(from, subject, to, body)
  }

}

/**
 * "Meta" trait that all concrete BaseUser implementations must
 * provide an implementation of to work with this package
 */
trait BaseUserMeta[T <: BaseUser[T]] {
  self: T =>
  
  // Real identity ------------------------------
  
  private object realUsernameVar extends SessionVar[Option[String]](Empty)
  private object realUserVar extends RequestVar[Option[T]](
    realUsernameVar.is.flatMap(byUsername _))

  def realUsername = realUsernameVar.is
  def realUser = realUserVar.is
  
  // Effective identity -------------------------
  
  private object effectiveUsernameVar extends SessionVar[Option[String]](Empty)
  private object effectiveUserVar extends RequestVar[Option[T]](
    effectiveUsernameVar.is.flatMap(byUsername _))

  def effectiveUsername = effectiveUsernameVar.is
  def effectiveUser = effectiveUserVar.is
  
  // User queries -------------------------------
  
  /**
   * Retrieve a user from the database by username. Override this to:
   *  - implement for various database backends;
   *  - use email addresses as usernames, or use separate usernames.
   *
   * Note the deviation from standard nomenclature. 
   * Most byFoo methods return query objects. We can't do that here because
   * we don't know what backend we're using. We therefore return an option instead. 
   */
  def byUsername(username: String): Option[T]
  
  /**
   * Retrieve a user from the database by email verification code. Override this to
   * implement for various database backends.
   *
   * Note the deviation from standard nomenclature. 
   * Most byFoo methods return query objects. We can't do that here because
   * we don't know what backend we're using. We therefore return an option instead. 
   */
  def byEmailVerificationCode(code: String): Option[T]

  /**
   * Retrieve a user from the database and check their password is correct.
   * Return Some(user) if the username and password are both correct, None
   * otherwise.
   *
   * Note the deviation from standard nomenclature. 
   * Most byFoo methods return query objects. We can't do that here because:
   *  - we don't know what backend we're using;
   *  - we have to do the password check outside of the DB.
   * We therefore return an option instead. 
   */
  def byUsernameAndPassword(username: String, password: String): Option[T] =
    byUsername(username).filter(_.password.match_?(password))
  
  // Password validation ------------------------
  
  /**
   * Does the supplied password meet the application's strength requirements for passwords?
   *
   * The default implementation checks that the password is >= 6 characters long.
   */
  def isValidPassword(password: String) =
    password.length >= 6
  
  /**
   * A message describing the application's strength requirements for passwords.
   *
   * Used to produce an error message if the user chooses a password that is too weak.
   */
  val invalidPasswordMessage =
    "Please enter six characters or more"

  // Log in, log out ----------------------------
  
  /** Is the user's effective identity logged in? */
  def isLoggedIn: Boolean =
    effectiveUsername.isDefined
  
  /** Is the user's real identity logged in? */
  def isReallyLoggedIn: Boolean =
    realUsername.isDefined
  
  /** 
   * Log the user in. Return true if successful, false if the username/password was wrong.
   * Does not inform the user in any way.
   * 
   * Both real and effective identities are set to the relevant username
   * (and user if the username is valid). If a user is already logged in, 
   * their real and effective identities are replaced by this new identity.
   */
  def logIn(username: String, password: String): Boolean =
    byUsernameAndPassword(username, password).
    headOption.
    filter(user => user.canLogIn).
    map(user => { logIn(user); true }).
    getOrElse(false)
  
  /** 
   * Log the user in. Return true if successful, false if the username/password was wrong.
   * Does not inform the user in any way.
   * 
   * On success, both real and effective identities are set to the relevant user.
   * If a user is already logged in, their real and effective identities are replaced
   * by this new identity.
   */
  def logIn(user: T): Unit =
    logIn(user.username.is)
  
  /** 
   * Log the user in.
   * 
   * Both real and effective identities are set to the relevant username
   * (and user if the username is valid). If a user is already logged in, 
   * their real and effective identities are replaced by this new identity.
   */
  def logIn(username: String): Unit = {
    realUserVar.remove
    effectiveUserVar.remove
    
    realUsernameVar.set(Some(username))
    effectiveUsernameVar.set(Some(username))
  }
  
  /** 
   * Log the user out. Real and effective identities are both set to None.
   * 
   * onLogOut callbacks are invoked afterwards, regardless of the 
   * success/failure of the login attempt. Override these to inform the user.
   */
  def logOut: Unit = {
    val real = realUser
    val effective = effectiveUser
    
    realUsernameVar.remove
    realUserVar.remove
    effectiveUsernameVar.remove
    effectiveUserVar.remove
  }
  
  // Change identity, restore identity ----------
  
  /**
   * If the User.realUser.canChangeIdentity, 
   * change the user's effective identity to the supplied user.
   * The user's real identity always remains the same.
   *
   * Returns true if the change was successful, false otherwise.
   */
  def changeIdentity(user: T): Boolean =
    realUser match {
      case Some(real) if real.canChangeIdentity =>
        effectiveUserVar.remove
        effectiveUsernameVar.set(Full(user.username.is))
        true
      
      case _ => false
    }
   
  /** (Re)sets the user's effective identity to be the same as their real identity. */
  def restoreIdentity: Unit = {
    effectiveUserVar.remove
    effectiveUsernameVar.set(realUsernameVar.get)
  }
  
}
