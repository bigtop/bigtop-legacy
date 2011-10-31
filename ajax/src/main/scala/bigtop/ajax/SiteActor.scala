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
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import java.util.UUID
import scala.collection.mutable._

case class SiteActor(val initialLifePoints: Int) extends Actor with ActorUtil with Logging {
  
  case class PageData(val actor: ActorRef, var lifePoints: Int)
  
  // TODO : Schedule Tick messages
  
  val pages: Map[UUID, PageData] = new HashMap
  
  def receive = {
    // Return true
    case msg @ Register(pageUuid, funcUuid, fn) =>
      println(this + " received " + msg)

      pages.getOrElseUpdate(
        pageUuid,
        PageData(Actor.actorOf(new PageActor(pageUuid)).start, initialLifePoints)
      ).actor.forward(msg)
    
    // Return Future[HttpResponse[S]]
    case msg @ Invoke(_, pageUuid, funcUuid, req) =>
      println(this + " received Invoke(" + pageUuid + ", " + funcUuid + ") " + pages)

      pages.get(pageUuid) match {
        case Some(data) => data.actor.forward(msg)
        case None       => self.reply(Future.sync(notFoundResponse))
      }

    // No return value
    case msg @ Heartbeat(pageUuid) =>
      println(this + " received " + msg)

      pages.get(pageUuid).foreach(_.lifePoints = initialLifePoints)

    // No return value
    case msg @ Tick =>
      println(this + " received " + msg)

      pages.foreach { case (uuid, data) =>
        if(data.lifePoints == 0) {
          pages.remove(uuid)
          data.actor.stop
        } else {
          data.lifePoints = data.lifePoints - 1
        }
      }
    
    // Return JValue
    case msg @ Status =>
      println(this + " received " + msg)

      val response: JObject =
        pages.toList.map { case (uuid, data) =>
          val dataJson: JObject =
            ("lifePoints" -> data.lifePoints) ~
            ("functions"  -> (data.actor !! Status).get.asInstanceOf[JValue])
          
          uuid.toString -> dataJson
        }.foldLeft(JObject.empty)(_ ~ _)

      self.reply(response)
    
    case other =>
      println(this + " received unknown message: " + other)
  }

  override def postStop = {
    super.postStop
    pages.foreach { case (uuid, data) =>
      data.actor.stop
    }
  }
  
}
