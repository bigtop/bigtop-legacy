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
package io

import akka.actor._
import akka.actor.Actor._
import akka.event.slf4j._
import blueeyes.concurrent._
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import java.util.UUID
import scala.collection._

case class PoolActor(
    val namespace: String,
    val heartbeatTimeout: Int,
    val connectionTimeout: Int,
    val initialLifePoints: Int
  ) extends Actor with ActorUtil with Logging {
  
  val supportedTransports =
    List("xhr-polling")
  
  val sessions = new mutable.HashMap[Uuid, SessionData]
  
  case class SessionData(val session: ActorRef, var lifePoints: Int)
  
  def receive = {
    
    // Return HandshakeResponse
    case Handshake =>
      val session: ActorRef =
        actorOf(new SessionActor(
          heartbeatTimeout = heartbeatTimeout,
          connectionTimeout = connectionTimeout,
          initialLifePoints = initialLifePoints
        )).start
    
      sessions.put(session.uuid, SessionData(session, initialLifePoints))
      
      self.reply(HandshakeResponse(
        session.uuid,
        heartbeatTimeout,
        connectionTimeout,
        supportedTransports
      ))
    
    // Return nothing
    case Tick =>
      sessions.foreach { case (uuid, data) => 
        if(data.lifePoints == 0) {
          sessions.remove(uuid)
          data.session.stop
        } else {
          data.lifePoints = data.lifePoints - 1
        }
      }

    // Return nothing
    case ToSession(sessionId, IoMessage.Heartbeat) =>
      sessions.get(sessionId).foreach(_.lifePoints = initialLifePoints)

    // Return IoMessage
    case ToSession(sessionId, msg) =>
      sessions.get(sessionId).
               map(data => data.session forward msg).
               // TODO : Add reason / advice
               getOrElse(self reply IoMessage.Error())
    
    // Return JValue
    case Status =>
      self reply {
        ("heartbeatTimeout"  -> heartbeatTimeout) ~
        ("connectionTimeout" -> connectionTimeout) ~
        ("initialLifePoints" -> initialLifePoints) ~
        ("sessions"          -> sessions.toList.map {
                                  case (uuid, data) =>
                                    (uuid.toString -> ("lifePoints" -> data.lifePoints))
                                }.foldLeft(JObject.empty)(_ ~ _))
      }

  }

  override def postStop = {
    super.postStop
    sessions.foreach { case (uuid, data) => data.session.stop }
  }
  
}
