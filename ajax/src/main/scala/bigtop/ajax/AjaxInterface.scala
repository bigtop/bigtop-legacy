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
import blueeyes.concurrent.Future._
import blueeyes.core.http._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.service._
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.util.DynamicVariable

object AjaxInterface {
  
  val _current = new DynamicVariable[Option[AjaxInterface]](None)

  def withInterface[T, S, V](interface: AjaxInterface)(body: => V): V =
    _current.withValue(Some(interface)) {
      body
    }
  
  def current: AjaxInterface =
    _current.value.getOrElse(throw new Exception("No AJAX interface in scope"))
  
}

case class AjaxInterface(val initialLifePoints: Int = 10, val tickDelayMillis: Int = 3000) {
  
  val _actor = Actor.actorOf(new SiteActor(initialLifePoints)).start
  val _currentPage = new DynamicVariable[Option[UUID]](None)
  
  Scheduler.schedule(
    _actor,
    Tick,
    tickDelayMillis,
    tickDelayMillis,
    TimeUnit.MILLISECONDS)

  def withPage[T](body: => T) = {
    val uuid = UUID.randomUUID
    _currentPage.withValue[T](Some(uuid)) { body }
  }
  
  def currentPage: UUID =
    _currentPage.value.getOrElse(throw new Exception("No page in scope"))
  
  def register[T, S](fn: HttpRequestHandler2[T, S]): UUID = {
    val pageUuid = currentPage
    val funcUuid = UUID.randomUUID
    val success  = (_actor !! Register(pageUuid, funcUuid, fn)).asInstanceOf[Boolean]
    
    if(success) funcUuid else throw new Exception("No page in scope")
  }
  
  def invoke[T, S](request: HttpRequest[T]): Future[HttpResponse[S]] = {
    val message =
      Invoke(UUID.fromString(request.parameters('pageUuid)),
             UUID.fromString(request.parameters('funcUuid)),
             request)
    
    ((_actor !!! message) map {
      response: HttpResponse[S] => response
    }).toBlueEyes
  }
  
  def heartbeat[T, S](request: HttpRequest[T]): Future[HttpResponse[S]] = {
    _actor !!! Heartbeat(UUID.fromString(request.parameters('pageUuid)))
    Future.sync(HttpResponse(HttpStatus(OK)))
  }
  
}
