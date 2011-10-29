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

package bigtop.ajax

import blueeyes.concurrent.Future
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.util._

trait AjaxRequestHandlerCombinators
    extends HttpRequestHandlerCombinators
    with RestPathPatternImplicits
    with PartialFunctionCombinators {
  
  import MimeTypes._
  
  /** ajax {
    *   ...
    * }
    *
    * expands to:
    * 
    * val site = actorOf(new SiteActor)
    * 
    * path(ajaxpath) {
    *   site ! Invoke(..., ..., ...)
    * } ~
    * withSite(site) {
    *   ...
    * }
    */
  def ajax[T, S] = {
    val interface = new AjaxInterface

    (handler: HttpRequestHandler2[T, S]) => {
      jvalue { case request: HttpRequest[JValue] =>
        path[T, S]("ajax" / 'pageUuid / "heartbeat") {
          get[T, S] { request => interface.heartbeat(request) }
        }
      } ~
      contentType(application/json) {
        path[T, S]("ajax" / 'pageUuid / 'funcUuid) {
          get[T, S] { request => interface.invoke(request) }
        }
      } ~
      new HttpRequestHandler2[T, S] {
        def isDefinedAt(request: HttpRequest[T]): Boolean =
          handler.isDefinedAt(request)

        def apply(request: HttpRequest[T]): Future[HttpResponse[S]] =
          AjaxInterface.withInterface(interface) {
            interface.withPage {
              handler(request)
            }
          }
      }
    }
  }
  
}