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
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.service._
import blueeyes.json.JsonAST._
import blueeyes.util._
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.util.DynamicVariable

object AjaxInterface {

  // Currently active interface -----------------
  
  val current = new DynamicVariable[Option[AjaxInterface]](None)

  def withInterface[T, S, V](interface: AjaxInterface)(body: => V): V =
    current.withValue(Some(interface)) { body }
  
  def currentInterface: AjaxInterface =
    current.value.getOrElse(throw new Exception("No AJAX interface in scope"))
  
  // Shortcuts to methods in AjaxInterface ------
  
  def currentPage =
    currentInterface.currentPage

  def register(fn: HttpRequestHandler[String])(implicit b: Bijection[ByteChunk, String]): UUID =
    currentInterface.register(
      new HttpRequestHandler[ByteChunk] {

        def isDefinedAt(req: HttpRequest[ByteChunk]): Boolean =
          fn.isDefinedAt(req.copy(content = req.content.map(b.apply _)))
        
        def apply(req: HttpRequest[ByteChunk]): Future[HttpResponse[ByteChunk]] =
          fn(req.copy(content = req.content.map(b.apply _))).
            map(res => res.copy(content = res.content.map(b.unapply _)))

      })
  
}

case class AjaxInterface(val initialLifePoints: Int = 10, val tickDelayMillis: Int = 3000)
    extends HttpRequestHandlerCombinators
    with BijectionsChunkJson
    with RestPathPatternImplicits
    with PartialFunctionCombinators {
  
  // Currently scoped page ----------------------
  
  private val current = new DynamicVariable[Option[UUID]](None)
  
  def withPage[T](body: => T): T =
    current.withValue(Some(UUID.randomUUID))(body)

  def withPage[T](pageUuid: UUID)(body: => T): T =
    current.withValue(Some(pageUuid))(body)
  
  def currentPage: UUID =
    current.value.getOrElse(throw new Exception("No page in scope"))
  
  // Actors and messages ------------------------
  
  private var _actor: Option[ActorRef] = None
  
  def actor =
    _actor.getOrElse(throw new Exception("AjaxInterface has not been started: " + this))
  
  def register(fn: HttpRequestHandler[ByteChunk]): UUID = {
    val funcUuid = UUID.randomUUID
    
    (actor !! Register(currentPage, funcUuid, fn)) match {
      case Some(true) => funcUuid
      case _          => throw new Exception("No page in scope")
    }
  }
  
  def invoke(pageUuid: UUID, funcUuid: UUID, request: HttpRequest[ByteChunk]): Future[HttpResponse[ByteChunk]] =
    (actor !! Invoke(this, pageUuid, funcUuid, request)).get.asInstanceOf[Future[HttpResponse[ByteChunk]]]
  
  def heartbeat(request: HttpRequest[JValue]): Future[HttpResponse[JValue]] = {
    actor ! Heartbeat(UUID.fromString(request.parameters('pageUuid)))
    
    Future.sync(HttpResponse(HttpStatus(OK)))
  }
  
  def status: JValue =
    (actor !! Status).get.asInstanceOf[JValue]
  
  // Blue Eyes service lifecycle ----------------
  
  def startup: Unit = {
    _actor = Some(Actor.actorOf(new SiteActor(initialLifePoints)).start)
    
    Scheduler.schedule(
      actor,
      Tick,
      tickDelayMillis,
      tickDelayMillis,
      TimeUnit.MILLISECONDS)
  }
  
  def handler[T](handler: HttpRequestHandler[T])(implicit tToJ: Bijection[T, JValue], tToB: Bijection[T, ByteChunk]): HttpRequestHandler[T] =
    path("/ajax/'pageUuid/heartbeat") {
      jvalue { (request: HttpRequest[JValue]) => heartbeat(request) }(tToJ)
    } ~
    path("/ajax/'pageUuid/'funcUuid") {
      new HttpRequestHandler[T] {
        def isDefinedAt(request: HttpRequest[T]) = true
        
        def apply(request: HttpRequest[T]): Future[HttpResponse[T]] = {
          val pageUuid = UUID.fromString(request.parameters('pageUuid))
          val funcUuid = UUID.fromString(request.parameters('funcUuid))
          convertResponse(invoke(pageUuid, funcUuid, convertRequest(request)(tToB)))(tToB)
        }
      }
    } ~
    new HttpRequestHandler[T] {
      def isDefinedAt(request: HttpRequest[T]) =
        handler.isDefinedAt(request)
      
      def apply(request: HttpRequest[T]): Future[HttpResponse[T]] =
        AjaxInterface.withInterface(AjaxInterface.this) {
          withPage {
            handler(request)
          }
        }
    }
  
  def shutdown: Unit = {
    val a = actor
    _actor = None
    a.stop
  }
  
  // Helpers ------------------------------------

  private[ajax] def convertRequest[A, B](req: HttpRequest[A])(implicit aToB: Bijection[A, B]): HttpRequest[B] =
    req.copy(content = req.content.map(aToB.apply))
  
  private[ajax] def convertResponse[A, B](future: Future[HttpResponse[B]])(implicit aToB: Bijection[A, B]): Future[HttpResponse[A]] =
    future.map(res => res.copy(content = res.content.map(aToB.unapply)))
  
}
