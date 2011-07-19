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

import org.scalatest._

import scala.xml.{NodeSeq,Utility,XML}
import net.liftweb.http.{S,LiftSession}
import net.liftweb.http.js.{JsCmd,JsCmds}
import net.liftweb.common.{Box,Empty}
import net.liftweb.util.Helpers._

import bigtop.record._

class BaseUserLoginSuite extends FunSuite with Assertions {

  val session = new LiftSession("", "01234567890123456789", Empty)

  def elems(css: String, seq: NodeSeq): NodeSeq = {
    var ans = NodeSeq.Empty
    val sel = (css) #> (in => { ans = ans ++ in; in })
    sel(seq)
    ans
  }

  test("BaseUserLogin.scaffold"){
    S.initIfUninitted(session) {
      val login = new BaseUserLogin(User)
      
      val output =
        login.scaffold(<ignored/>)
      
      val inputs = output \\ "input"

      expect(4) {
        elems("input", output).toList.length
      }
      
      elems("data-login-binding=username", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "text")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 username element: " + output)
      }
      
      elems("data-login-binding=password", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "password")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 password element: " + output)
      }
      
      elems("data-login-binding=submit", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "submit")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 submit element: " + output)
      }
      
      elems("data-login-binding=forgot-password", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "submit")
          assert((head \ "@class").toString == "forgot-password")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 forgot-password element: " + output)
      }
    }
  }

  test("BaseUserLogin.render"){
    S.initIfUninitted(session) {
      val login = new BaseUserLogin(User)
      
      val output =
        login.render(
          <div>
            <input data-login-binding="username" name="username" class="foo" />
            <input data-login-binding="password" name="password" class="bar"/>
            <input data-login-binding="submit" type="submit" class="baz"/>
          </div>)
      
      val inputs = output \\ "input"

      expect(3) {
        elems("input", output).toList.length
      }
      
      elems("data-login-binding=username", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "text")
          assert((head \ "@class").toString == "foo")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 username element: " + output)
      }
      
      elems("data-login-binding=password", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "password")
          assert((head \ "@class").toString == "bar")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 password element: " + output)
      }
      
      elems("data-login-binding=submit", output).toList match {
        case head :: Nil =>
          assert((head \ "@type").toString == "submit")
          assert((head \ "@class").toString == "baz")
          assert((head \ "@name").toString.length > 16)
        case list =>
          fail("Found <> 1 submit element: " + output)
      }
    }
  }
  
}
