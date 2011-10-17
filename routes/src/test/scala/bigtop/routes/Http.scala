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
package routes

import java.net.URLDecoder.{decode => urlDecode}
import net.liftweb.common.Empty
import net.liftweb.http._
import org.specs.matcher.Matcher

object TestRequest {

  def apply(path: String): Req =
    apply(path.split("/").
               toList.
               map(urlDecode(_, "utf-8").trim).
               filterNot(_ == ""))
  
  def apply(path: List[String]): Req =
    new Req(_path            = new ParsePath(partPath = path,
                                             suffix = "",
                                             absolute = true,
                                             endSlash = false),
            _contextPath     = "",
            _requestType     = GetRequest,
            _contentType     = Some("multipart/form-data"),
            _request         = null,      // TODO : Put something less volatile here!
            _nanoStart       = 0L,
            _nanoEnd         = 0L,
            _paramCalculator = () ⇒ new ParamCalcInfo(paramNames    = Nil,
                                                      params        = Map(),
                                                      uploadedFiles = Nil,
                                                      body          = Empty),
            _addlParams      = Map[String, String]())
  
}

object TestResponse {
  
  def apply(content: String): LiftResponse =
    new InMemoryResponse(data    = content.getBytes, 
                         headers = Nil,
                         cookies = Nil,
                         code    = 200)
  
}

case class matchResponse(a: LiftResponse) extends Matcher[LiftResponse]() {
  def apply(b: => LiftResponse) = {
    val matched =
      (a, b) match {
        case (a: InMemoryResponse, b: InMemoryResponse) =>
          a.code == b.code &&
          a.headers == b.headers &&
          a.cookies == b.cookies &&
          new String(b.data, "UTF-8") == new String(b.data, "UTF-8")
      
        case _ => false
      }

    (matched, "responses matched", "responses did not match")
  }
}
