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

import blueeyes.concurrent.Future
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

class AjaxInterfaceSpec
    extends Specification 
    with HttpRequestHandlerCombinators 
    with BijectionsChunkString
    with FutureMatchers
    with RestPathPatternImplicits {
  
  override implicit val defaultFutureTimeouts: FutureTimeouts = FutureTimeouts(0, 100L.milliseconds)
  
  // Helpers ------------------------------------

  implicit val JValueToString = new Bijection[JValue, String] {
    def apply(s: JValue)   = Printer.compact(Printer.render(s))
    def unapply(t: String) = JsonParser.parse(t)
  }

  implicit val StringToJValue = JValueToString.inverse
  
  // Tests --------------------------------------
  
  "ajax interface" should {
  
    def createCallback(content: String): HttpRequestHandler[String] =
      (request: HttpRequest[String]) =>
        Future.sync(HttpResponse[String](content = Some(AjaxInterface.currentPage + " " + content)))

    val interface = AjaxInterface(5, 100)
  
    val site: HttpRequestHandler[String] =
      interface.handler {
        path("/") {
          get { request =>
            Future.sync(HttpResponse(content =
              Some(List(AjaxInterface.currentPage,
                        AjaxInterface.register(createCallback("callback 1")),
                        AjaxInterface.register(createCallback("callback 2"))).mkString(" "))))
          }
        }
      }
    
    doBefore(interface.startup)
    doAfter(interface.shutdown)
  
    "ajax combinator has the right type" in {
      site mustBe site
    }
  
    "interface starts off empty" in {
      interface.status mustEqual JObject.empty
    }
  
    "user code can register callbacks" in {
      site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
        verify {
          (res: HttpResponse[String]) => {
            val status: JValue =
              interface.status
            
            val uuids @ (pageUuid :: funcUuid1 :: funcUuid2 :: Nil) =
              res.content.get.split(" ").toList
            
            val expectedStatus: JValue =
              (pageUuid -> (("lifePoints" -> 5) ~
                            ("functions"  -> List(funcUuid1, funcUuid2))))
            
            println("ACTUAL STATUS " + Printer.pretty(Printer.render(status)))
            println("EXPECTED STATUS " + Printer.pretty(Printer.render(expectedStatus)))
            
            // The 3 UUIDs must all be different:
            uuids.distinct.length mustEqual 3
          
            // Interface must contain one page and two functions:
            status mustEqual expectedStatus

            site(HttpRequest[String](method = HttpMethods.GET, uri = "/ajax/" + pageUuid + "/" + funcUuid1)) must whenDelivered {
              verify { 
                (res: HttpResponse[String]) => {
                  println("Callback 1 " + res)
                  res.content must beSome(pageUuid + " callback 1")
                }
              }
            }

            site(HttpRequest[String](method = HttpMethods.GET, uri = "/ajax/" + pageUuid + "/" + funcUuid2)) must whenDelivered {
              verify {
                (res: HttpResponse[String]) => {
                  println("Callback 2 " + res)
                  res.content must beSome(pageUuid + " callback 2")
                }
              }
            }
          }
        }
      }
    }

    "different invocations of user code result in different page UUIDs" in {
      site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
        verify {
          (res: HttpResponse[String]) => {
            val content1: List[String] =
              res.content.get.split(" ").toList

              site(HttpRequest[String](method = HttpMethods.GET, uri = "/")) must whenDelivered {
                verify {
                  (res: HttpResponse[String]) => {
                    val content2: List[String] =
                      res.content.get.split(" ").toList
                    
                    // The 6 UUIDs must all be different:
                    (content1 ::: content2).distinct.length mustEqual 6
                  }
                }
              }
          }
        }
      }
    }
    
  }

}