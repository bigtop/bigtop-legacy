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
package snippet

import scala.xml._

import net.liftweb.common._
import net.liftweb.http.{S,SHtml,StatefulSnippet}
import net.liftweb.http.js.{JsCmd,JsCmds}
import net.liftweb.util._
import net.liftweb.util.Helpers._

import bigtop.record._
import bigtop.snippet.SnippetUtil._

/** Handles BaseUser password resets. */
class BaseUserResetPassword[T <: BaseUser[T]](meta: BaseUserMeta[T]) extends StatefulSnippet with Loggable {
  
  /** ONE new password (ah ha ha haaa) */
  var password = ""
  
  /** TWO new password (ah ha ha haaa */
  var confirmPassword = ""
  
  /** Label to put on the submit button. */
  val submitLabel = "Reset my password"
  
  /** The user whose password we're changing. */
  val user: T =
    meta.byEmailVerificationCode(S.param("code").getOrElse("none")).
         getOrElse(S.redirectTo("/"))
  
  def dispatch = { 
    case "render" => render
    case "scaffold" => scaffold
  }
  
  def render =
    bindPassword &
    bindConfirmPassword &
    bindSubmit
  
  def scaffold(in: NodeSeq) =
    render(scaffoldTemplate)
  
  def scaffoldTemplate =
    <form method="post" class="reset-password-form">
      <div class="field-wrapper">
        <label for="password">Enter a new password</label>
        <input data-reset-binding="password" type="password" />
        <div class="message-wrapper">
          <div class="lift:Msg?id=password_id"></div>
        </div>
      </div>
      <div class="field-wrapper">
        <label for="confirm-password">Confirm your password</label>
        <input data-reset-binding="confirm-password" type="password" />
        <div class="message-wrapper">
          <div class="lift:Msg?id=confirmPassword_id"></div>
        </div>
      </div>
      <div class="submit-wrapper">
        { S.param("go").map(go => <input type="hidden" name="go" value={go} />).getOrElse(NodeSeq.Empty) }
        <input data-reset-binding="submit" type="submit" />
      </div>
    </form>
  
  def bindPassword =
    "data-reset-binding=password" #> 
    preserveAttrs(SHtml.password(password, onPasswordChange _), "name" :: Nil)
  
  def bindConfirmPassword =
    "data-reset-binding=confirm-password" #> 
    preserveAttrs(SHtml.password(password, onConfirmPasswordChange _), "name" :: Nil)
  
  def bindSubmit =
    "data-reset-binding=submit" #> 
    preserveAttrs(SHtml.submit(submitLabel, onSubmit _), "name" :: Nil)
  
  def onPasswordChange(str: String): Unit =
    password = str.trim
  
  def onConfirmPasswordChange(str: String): Unit =
    confirmPassword = str.trim
  
  def onSubmit: Unit =
    if(password != confirmPassword) {
      S.error("password_id", Text(""))
      S.error("confirmPassword_id", Text("Your passwords do not match"))
      failure
    } else if(!meta.isValidPassword(password)) {
      S.error("password_id", meta.invalidPasswordMessage)
      S.error("confirmPassword_id", Text(""))
      failure
    } else {
      S.error("password_id", Text(""))
      S.error("confirmPassword_id", Text(""))
      
      user.emailVerificationCode(None).emailVerified(true).password(password).validate match {
        case Nil =>
          user.save
          success
        
        case errors =>
          S.error("Password reset failed")
          S.error(errors)
          failure
      }
    }

  /** Action to take after the user's password has been changed. */
  def success: Unit =
    S.redirectTo(S.param("go").openOr("/"))

  /** Action to take if the user's password could not be changed. */
  def failure: Unit = {}
    
}
