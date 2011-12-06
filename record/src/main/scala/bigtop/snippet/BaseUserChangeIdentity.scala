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

/** Handles BaseUser change identity. */
class BaseUserChangeIdentity[T <: BaseUser[T]](meta: BaseUserMeta[T]) extends StatefulSnippet with Loggable {
  
  /** The username */
  var username = ""
  
  def dispatch = { 
    case "render" => render
    case "scaffold" => scaffold
  }
  
  def render =
    bindUsername &
    bindSubmit
  
  def scaffold =
    "*" #> render(scaffoldTemplate)
  
  def scaffoldTemplate =
    <form method="post" class="change-identity-form">
      <div class="field-wrapper">
        <label for="username">{ scaffoldUsernameLabel }</label>
        <input data-change-identity-binding="username" type="text" />
        <div class="message-wrapper">
          <div class="lift:Msg?id=username_id"></div>
        </div>
      </div>
      <div class="submit-wrapper">
        { S.param("go").map(go => <input type="hidden" name="go" value={go} />).getOrElse(NodeSeq.Empty) }
        <input data-change-identity-binding="submit" type="submit" class="submit" />
      </div>
    </form>
  
  def scaffoldUsernameLabel: NodeSeq =
    Text("Username")
  
  def bindUsername =
    "data-change-identity-binding=username" #> 
    preserveAttrs(SHtml.text(username, onUsernameChange _), "name" :: Nil)
  
  def bindSubmit =
    "data-change-identity-binding=submit" #> 
     preserveAttrs(SHtml.submit("Log in", onSubmit _), "name" :: Nil)
  
  def onUsernameChange(str: String): Unit =
    username = str.trim
  
  def onSubmit: Unit =
    if(meta.effectiveUser.map(_.canChangeIdentity).getOrElse(false)) {
      meta.byUsername(username) match {
        case Some(user) =>
          if(meta.changeIdentity(user)) success else failure
        
        case _ =>
          S.notice("That user does not exist.")
          failure
      }
    } else {
      failure
    }
  
  /** Action to take after the user has successfully logged in. */
  def success: Unit =
    S.redirectTo(S.param("go").openOr("/"))

  /** Action to take if the user could not be logged in. */
  def failure: Unit = {}
    
}
