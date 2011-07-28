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

/**
 * Handles BaseUser login.
 */
class BaseUserLogin[T <: BaseUser[T]](meta: BaseUserMeta[T]) extends StatefulSnippet with Loggable {
  
  /** The username */
  var username = ""
  
  /** The user's password */
  var password = ""

  def dispatch = { 
    case "render" => render
    case "scaffold" => scaffold
  }
  
  def render =
    bindUsername &
    bindPassword &
    bindSubmit &
    bindForgotPassword
  
  def scaffold(in: NodeSeq) =
    render(scaffoldTemplate)
  
  def scaffoldTemplate =
    <form method="post" class="login-form">
      <div class="field-wrapper">
        <label for="username">{ scaffoldUsernameLabel }</label>
        <input data-login-binding="username" type="text" />
        <div class="message-wrapper">
          <div class="lift:Msg?id=username_id"></div>
        </div>
      </div>
      <div class="field-wrapper">
        <label for="password">Password</label>
        <input data-login-binding="password" type="password" />
        <div class="message-wrapper">
          <div class="lift:Msg?id=password_id"></div>
        </div>
      </div>
      <div class="submit-wrapper">
        { S.param("go").map(go => <input type="hidden" name="go" value={go} />).getOrElse(NodeSeq.Empty) }
        <input data-login-binding="submit" type="submit" class="submit" />
        <input data-login-binding="forgot-password" type="submit" class="forgot-password" />
      </div>
    </form>
  
  def scaffoldUsernameLabel: NodeSeq =
    Text("Username")
  
  def bindUsername =
    "data-login-binding=username" #> 
    preserveAttrs(SHtml.text(username, onUsernameChange _), "name" :: Nil)
  
  def bindPassword =
    "data-login-binding=password" #> 
    preserveAttrs(SHtml.password(password, onPasswordChange _), "name" :: Nil)
  
  def bindSubmit =
    "data-login-binding=submit" #> 
     preserveAttrs(SHtml.submit("Log in", onSubmit _), "name" :: Nil)
  
  def bindForgotPassword =
    "data-login-binding=forgot-password" #>
    preserveAttrs(SHtml.submit("Forgot password", onForgotPassword _), "name" :: Nil)
  
  def onUsernameChange(str: String): Unit =
    username = str.trim
  
  def onPasswordChange(str: String): Unit =
    password = str.trim
  
  def onSubmit: Unit =
    if(meta.logIn(username, password)) success else failure

  /** Action to take after the user has successfully logged in. */
  def success: Unit = {
    S.notice("Hello again, " + meta.effectiveUser.map(_.username.is).getOrElse("Anonymous") + ".")
    S.redirectTo(S.param("go").openOr("/"))
  }

  /** Action to take if the user could not be logged in. */
  def failure: Unit =
    S.error("Login details incorrect.")

  def onForgotPassword: Unit =
    if(username.length > 0) {
      meta.byUsername(username).foreach(_.sendForgotPasswordEmail)
      S.notice("We have sent you an email containing instructions on how to reset your password.")
    } else {
      S.error("Please enter your username and click \"Forgot password\" again.")
    }
    
}
