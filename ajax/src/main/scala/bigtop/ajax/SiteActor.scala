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
import java.util.UUID
import scala.collection.mutable._

class SiteActor(val initialLifePoints: Int) extends Actor {
  
  case class PageData(val actor: ActorRef, var lifePoints: Int)
  
  // TODO : Schedule Tick messages
  
  val pages: Map[UUID, PageData] = new HashMap
  
  def receive = {
    // Return true
    case Register(pageUuid, funcUuid, fn) =>
      val data =
        PageData(
          Actor.actorOf(new PageActor).start,
          initialLifePoints)
      
      pages.put(pageUuid, data)
      self.forward(data.actor)
    
    // Return Future[HttpResponse[S]]
    case Invoke(pageUuid, funcUuid, req) =>
      pages.get(pageUuid) match {
        case Some(data) =>
          self.forward(data.actor)
        
        case None =>
          self.reply(Future.sync(HttpResponse(HttpStatus(NotFound))))
      }

    // No return value
    case Heartbeat(pageUuid) =>
      pages.get(pageUuid).foreach(_.lifePoints = initialLifePoints)

    // No return value
    case Tick =>
      pages.foreach { case (uuid, data) =>
        if(data.lifePoints == 0) {
          pages.remove(uuid)
          data.actor ! Kill
        } else {
          data.lifePoints = data.lifePoints - 1
        }
      }
  }
  
}
