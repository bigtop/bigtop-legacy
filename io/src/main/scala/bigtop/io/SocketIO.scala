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

object SocketIO {

  // Currently active interface -----------------
  
  val current = new DynamicVariable[Option[SocketIO]](None)

  def withInterface[T, S, V](interface: SocketIO)(body: => V): V =
    current.withValue(Some(interface)) { body }
  
  def currentInterface: SocketIO =
    current.value.getOrElse(throw new Exception("No AJAX interface in scope"))
  
  // Shortcuts to methods in SocketIO ------
  
  // def currentPage =
  //   currentInterface.currentPage
  // 
  // def register(fn: HttpRequestHandler[String])(implicit b: Bijection[ByteChunk, String]): UUID =
  //   currentInterface.register(
  //     new HttpRequestHandler[ByteChunk] {
  // 
  //       def isDefinedAt(req: HttpRequest[ByteChunk]): Boolean =
  //         fn.isDefinedAt(req.copy(content = req.content.map(b.apply _)))
  //       
  //       def apply(req: HttpRequest[ByteChunk]): Future[HttpResponse[ByteChunk]] =
  //         fn(req.copy(content = req.content.map(b.apply _))).
  //           map(res => res.copy(content = res.content.map(b.unapply _)))
  // 
  //     })
  
}

case class SocketIO(
    val namespace: String = "socket.io",
    val heartbeatTimeout: Int = 2,
    val connectionTimeout: Int = 1,
    val initialLifePoints: Int = 10
  ) extends HttpRequestHandlerCombinators
    with BijectionsChunkJson
    with RestPathPatternImplicits
    with PartialFunctionCombinators {
  
  // Actors and messages ------------------------
  
  var _pool: Option[ActorRef] =
    None
  
  def pool =
    _pool.getOrElse(throw new Exception("SocketIO interface has not been started"))
  
  def status: JValue =
    (pool !!! Status).get.asInstanceOf[JValue]
  
  // Blue Eyes service lifecycle ----------------
  
  def startup: Unit = {
    _pool = Some(actorOf(new PoolActor(
      namespace = namespace,
      heartbeatTimeout = heartbeatTimeout,
      connectionTimeout = connectionTimeout,
      initialLifePoints = initialLifePoints
    )).start)
    
    Scheduler.schedule(
      pool,
      Tick,
      1000 * (heartbeatTimeout + connectionTimeout) / initialLifePoints,
      1000 * (heartbeatTimeout + connectionTimeout) / initialLifePoints,
      TimeUnit.MILLISECONDS)
  }
  
  def handler[T](handler: HttpRequestHandler[T])(implicit tToJ: Bijection[T, JValue], tToS: Bijection[T, String]): HttpRequestHandler[T] =
    // Handshake endpoint:
    path("/%s/1/".format(namespace)) {
      withConversion {
        get { request: HttpRequest[String] =>
          (pool !!! Handshake) map (xhrPollingResponse _) toBlueEyes
        }
      }(tToS)
    } ~
    // XHR polling endpoint:
    path("/%s/1/xhr-polling/'sessionId/".format(namespace)) {
      withConversion {
        get { req: HttpRequest[String] => 
          val sessionId = new Uuid(req.parameters('sessionId))
          (pool !!! ToSession(sessionId, IoMessage.Noop)) map (xhrPollingResponse _) toBlueEyes
        } ~
        post { req: HttpRequest[String] =>
          val sessionId = new Uuid(req.parameters('sessionId))
          req.content match {
            case Some(IoMessage(msg)) =>
              (pool !!! ToSession(sessionId, msg)) map (xhrPollingResponse _) toBlueEyes

            case _ => sys.error("Not implemented yet")
          }
        }
      }(tToS)
    } ~
    new HttpRequestHandler[T] {
      def isDefinedAt(request: HttpRequest[T]) =
        handler.isDefinedAt(request)
      
      def apply(request: HttpRequest[T]): Future[HttpResponse[T]] =
        SocketIO.withInterface(SocketIO.this) {
          handler(request)
        }
    }
  
  def shutdown: Unit = {
    val p = pool
    _pool = None
    p.stop
    
    // TODO : Clean up scheduler
    println("shutdown() doesn't yet clean up scheduled Tick events")
  }
  
  // Helpers ------------------------------------
  
  private[io] def withConversion[A, B](handler: HttpRequestHandler[B])(implicit aToB: Bijection[A, B]): HttpRequestHandler[A] =
    (req: HttpRequest[A]) =>
      convertResponse(handler(convertRequest(req)(aToB)))(aToB)
  
  private[io] def convertRequest[A, B](req: HttpRequest[A])(implicit aToB: Bijection[A, B]): HttpRequest[B] =
    req.copy(content = req.content.map(aToB.apply))
  
  private[io] def convertResponse[A, B](future: Future[HttpResponse[B]])(implicit aToB: Bijection[A, B]): Future[HttpResponse[A]] =
    future.map(res => res.copy(content = res.content.map(aToB.unapply)))
  
  def xhrPollingResponse(msg: Any): HttpResponse[String] =
    msg match {
      case msg: HandshakeResponse =>
        HttpResponse[String](
          status = HttpStatus(OK),
          content = Some(msg.toString)
        )
        
      case msg: IoMessage =>
        HttpResponse[String](
          status  = HttpStatus(OK),
          content = Some(msg.toString)
        )
    
      case other =>
        HttpResponse[String](
          status  = HttpStatus(InternalServerError),
          content = None
        )
    }

}
