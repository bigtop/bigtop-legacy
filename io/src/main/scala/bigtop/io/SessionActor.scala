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
import akka.event.slf4j._
import blueeyes.concurrent._
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import java.util.UUID
import scala.collection.mutable._

case class SessionActor(
    val heartbeatTimeout: Int,
    val connectionTimeout: Int,
    val initialLifePoints: Int
  ) extends Actor with ActorUtil with Logging {
  
  def receive = {
    // Return JValue
    case Status =>
      self.reply(JObject.empty)
      
    case msg =>
      println("SessionActor " + this + "\n    received " + msg)
  }

}
