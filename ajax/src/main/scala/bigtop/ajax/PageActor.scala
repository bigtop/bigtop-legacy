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
import akka.event.slf4j._
import blueeyes.concurrent._
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import java.util.UUID
import scala.collection.mutable._

case class PageActor(val pageUuid: UUID) extends Actor with ActorUtil with Logging {
  
  val functions: Map[UUID, HttpRequestHandler[ByteChunk]] = new HashMap
  
  def receive = {
    // Return true
    case msg @ Register(_, funcUuid, fn) =>
      functions.put(funcUuid, fn)
      println(this + " received " + msg + " " + functions)
      self.reply(true)
    
    // Return Future[HttpResponse[S]]
    case msg @ Invoke(interface, pageUuid, funcUuid, request) =>
      println(this + " received Invoke(" + funcUuid + ") " + functions)

      val response: Future[HttpResponse[ByteChunk]] =
        functions.
          get(funcUuid).
          map { func =>
            AjaxInterface.withInterface(interface) {
              interface.withPage(pageUuid) {
                func(request)
              }
            }
          }.getOrElse(Future.sync(notFoundResponse))
      
      self.reply(response)
    
    // Return JValue
    case msg @ Status =>
      println(this + " received " + msg)

      val response: JArray =
        functions.toList.map { case (uuid, data) =>
          JString(uuid.toString)
        }

      self.reply(response)
    
    case other =>
      println(this + " received unknown message: " + other)
  }

}
