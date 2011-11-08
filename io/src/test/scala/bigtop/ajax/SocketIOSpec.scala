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

import blueeyes.concurrent._
import blueeyes.concurrent.Future._
import blueeyes.concurrent.test.FutureMatchers
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.core.data.BijectionsIdentity
import blueeyes.json._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import blueeyes.util.metrics.Duration._
import org.specs.Specification
import org.specs.matcher.Matchers._

class SocketIOSpec
    extends Specification 
    with HttpRequestHandlerCombinators 
    with BijectionsChunkString
    with FutureMatchers
    with RestPathPatternImplicits {
  
  noDetailedDiffs
  
  override implicit val defaultFutureTimeouts: FutureTimeouts =
    FutureTimeouts(0, 100L.milliseconds)

  // Helpers ------------------------------------
  
  implicit val JValueToString = new Bijection[JValue, String] {
    def apply(s: JValue)   = Printer.compact(Printer.render(s))
    def unapply(t: String) = JsonParser.parse(t)
  }
  
  implicit val StringToJValue = JValueToString.inverse
  
  type Req = HttpRequest[String]
  type Res = HttpResponse[String]
  
  class PimpedJValue(value: JValue) {
  
    def asString: String =
      (value --> classOf[JString]).value
  
    def asInt: Int =
      (value --> classOf[JInt]).value.toInt
    
    def fieldNames: List[String] =
      (value --> classOf[JObject]).fields.map(_.name)
  
  }
  
  implicit def pimpJValue(value: JValue): PimpedJValue =
    new PimpedJValue(value)

  // Test data ----------------------------------

  val io = SocketIO(heartbeatTimeout = 1, connectionTimeout = 1)
  
  val site: HttpRequestHandler[String] =
    io.handler {
      path("/") {
        get[String, String] {
          request =>
            println("MAIN SITE")
            HttpResponse(content = Some("page")).future
        }
      }
    }
  
  def getRequest(uri: String) =
    HttpRequest[String](method = HttpMethods.GET, uri = uri)
  
  "bigtop.io" should {
     
    doBefore(io.startup)
    doAfter(io.shutdown)
    
    "accept a handshake" in {
      site(getRequest("/socket.io/1/")) must whenDelivered {
        verify { (res: Res) => 
          
          val sessionId = 
            res.content flatMap (HandshakeResponse.apply _) match {
              case Some(HandshakeResponse(uuid, heartbeatTimeout, connectionTimeout, supportedTransports)) =>
                heartbeatTimeout mustEqual 1
                connectionTimeout mustEqual 1
                supportedTransports mustEqual List("xhr-polling")
                uuid
              
              case other =>
                fail("unexpected response: " + other)
            }
          
          val status = io.status
          (status \ "heartbeatTimeout" asInt) mustEqual 1
          (status \ "connectionTimeout" asInt) mustEqual 1
          (status \ "sessions" fieldNames) mustEqual List(sessionId.toString)
          // Life points should start at 10... we check for > 8 to avoid timing issues:
          (status \ "sessions" \ sessionId.toString \ "lifePoints" asInt) must beGreaterThan(8)
        }
      }
    }
    
    "clean up unused sessions" in {
      site(getRequest("/socket.io/1/")) must whenDelivered {
        verify { (res: Res) => 
          
          val sessionId = 
            res.content flatMap (HandshakeResponse.apply _) match {
              case Some(HandshakeResponse(uuid, _, _, _)) => uuid
              case _ => fail("no session found")
            }
          
          // Life points should start at 10... we check for > 8 to avoid timing issues:
          (io.status \ "sessions" \ sessionId.toString \ "lifePoints" asInt) must beGreaterThan(8)
          (io.status \ "sessions" fieldNames) must notBeEmpty
          
          // Life points should count down and the pool should eventually drop all sessions:
          (io.status \ "sessions" \ sessionId.toString \ "lifePoints" asInt) must eventually(beLessThan(10))
          (io.status \ "sessions" fieldNames) must eventually(beEmpty)
        }
      }
    }
    
    "preserve sessions that are receiving heartbeats" in {
      site(getRequest("/socket.io/1/")) must whenDelivered {
        verify { (res: Res) => 
          
          val sessionId = 
            res.content flatMap (HandshakeResponse.apply _) match {
              case Some(HandshakeResponse(uuid, _, _, _)) => uuid
              case _ => fail("no session found")
            }
          
          // Life points should start at 10... we check for > 8 to avoid timing issues:
          (io.status \ "sessions" \ sessionId.toString \ "lifePoints" asInt) must beGreaterThan(8)
          (io.status \ "sessions" fieldNames) must notBeEmpty
          
          site(postRequest("/socket.io/1/" + sessionId.toString + "/xhr-polling")) must whenDelivered {
            verify { 
              for(repeat <- 1 to 10) {
                
              }
            }
          }
          
          // Life points should count down and the pool should eventually drop all sessions:
          (io.status \ "sessions" \ sessionId.toString \ "lifePoints" asInt) must eventually(beLessThan(10))
          (io.status \ "sessions" fieldNames) must eventually(beEmpty)
        }
      }
    }
  }
  
  // // Tests --------------------------------------
  // 
  // "ajax interface" should {
  // 
  //   def createCallback(content: String): HttpRequestHandler[String] =
  //     (request: HttpRequest[String]) =>
  //       Future.sync(HttpResponse[String](content = Some(SocketIO.currentPage + " " + content)))
  // 
  //   val interface = SocketIO(5, 100)
  // 
  //   val site: HttpRequestHandler[String] =
  //     interface.handler {
  //       path("/") {
  //         get { request =>
  //           Future.sync(HttpResponse(content =
  //             Some(List(SocketIO.currentPage,
  //                       SocketIO.register(createCallback("callback 1")),
  //                       SocketIO.register(createCallback("callback 2"))).mkString(" "))))
  //         }
  //       }
  //     }
  //   
  //   doBefore(interface.startup)
  //   doAfter(interface.shutdown)
  // 
  //   "ajax combinator has the right type" in {
  //     site mustBe site
  //   }
  // 
  //   "interface starts off empty" in {
  //     interface.status mustEqual JObject.empty
  //   }
  // 
  //   "user code can register callbacks" in {
  //     site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
  //       verify {
  //         (res: HttpResponse[String]) => {
  //           val status: JValue =
  //             interface.status
  //           
  //           val uuids @ (pageUuid :: funcUuid1 :: funcUuid2 :: Nil) =
  //             res.content.get.split(" ").toList
  //           
  //           val expectedStatus: JValue =
  //             (pageUuid -> (("lifePoints" -> 5) ~
  //                           ("functions"  -> List(funcUuid1, funcUuid2))))
  //           
  //           println("ACTUAL STATUS " + Printer.pretty(Printer.render(status)))
  //           println("EXPECTED STATUS " + Printer.pretty(Printer.render(expectedStatus)))
  //           
  //           // The 3 UUIDs must all be different:
  //           uuids.distinct.length mustEqual 3
  //         
  //           // Interface must contain one page and two functions:
  //           status mustEqual expectedStatus
  // 
  //           site(HttpRequest[String](method = HttpMethods.GET, uri = "/ajax/" + pageUuid + "/" + funcUuid1)) must whenDelivered {
  //             verify { 
  //               (res: HttpResponse[String]) => {
  //                 println("Callback 1 " + res)
  //                 res.content must beSome(pageUuid + " callback 1")
  //               }
  //             }
  //           }
  // 
  //           site(HttpRequest[String](method = HttpMethods.GET, uri = "/ajax/" + pageUuid + "/" + funcUuid2)) must whenDelivered {
  //             verify {
  //               (res: HttpResponse[String]) => {
  //                 println("Callback 2 " + res)
  //                 res.content must beSome(pageUuid + " callback 2")
  //               }
  //             }
  //           }
  //         }
  //       }
  //     }
  //   }
  // 
  //   "different invocations of user code result in different page UUIDs" in {
  //     site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
  //       verify {
  //         (res: HttpResponse[String]) => {
  //           val content1: List[String] =
  //             res.content.get.split(" ").toList
  // 
  //             site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
  //               verify {
  //                 (res: HttpResponse[String]) => {
  //                   val content2: List[String] =
  //                     res.content.get.split(" ").toList
  //                   
  //                   // The 6 UUIDs must all be different:
  //                   (content1 ::: content2).distinct.length mustEqual 6
  //                 }
  //               }
  //             }
  //         }
  //       }
  //     }
  //   }
  //   
  // }

}