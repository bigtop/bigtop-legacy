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
package ajax

import akka.actor._
import blueeyes.concurrent._
import blueeyes.core.http._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.service._
import java.util.UUID
import scala.collection.mutable._

class PageActor extends Actor {
  
  val functions: Map[UUID, HttpRequestHandler2[_, _]] = new HashMap
  
  def receive = {
    // Return true
    case Register(_, uuid, fn) =>
      functions.put(uuid, fn)
      self.reply(true)
    
    // Return Future[HttpResponse[S]]
    case Invoke(_, uuid, request) =>
      val response: Future[HttpResponse[_]] =
        functions.get(uuid).
                  map(_(request)).
                  getOrElse(Future.sync(HttpResponse(HttpStatus(NotFound))))
      
      self.reply(response)
  }
  
}
