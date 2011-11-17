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

package bigtop.routes.scalatra

import bigtop.core.Url
import javax.servlet._
import javax.servlet.http._
import org.scalatra._
import org.specs._
import scala.util.DynamicVariable

class SiteSpec extends Specification with bigtop.routes.plain.Http {
  
  noDetailedDiffs
  
  object Calculator extends Site {
  
    // Routing table:

    val add      = ("add"      :/: IntArg    :/: "to"   :/: IntArg  :/: end) >> (doAdd _)
    val multiply = ("multiply" :/: IntArg    :/: "by"   :/: IntArg  :/: end) >> (doMultiply _)
    val square   = ("square"   :/: IntArg                           :/: end) >> (doSquare _)
    val repeat   = ("repeat"   :/: StringArg :/: IntArg :/: "times" :/: end) >> (doRepeat _)
    val append   = ("append"                                        :/: any) >> (doAppend _)

    // Implementation:

    def doAdd(a: Int, b: Int): Any =
      "%s + %s = %s".format(a, b, a + b)
  
    def doMultiply(a: Int, b: Int): Any =
      "%s * %s = %s".format(a, b, a * b)
    
    def doSquare(a: Int): Any =
      multiply(a, a)

    def doRepeat(a: String, b: Int): Any =
      "%s * %s = %s".format(a, b, a * b)
  
    def doAppend(a: List[String]): Any =
      "append(%s) = %s".format(a, a.mkString)
    
    // Hooks for tests:
    
    /** Invokes the top-level apply() method and returns the accumulated response content. */
    def testTopLevel(req: TestRequest): String = {
      val res = new TestResponse

      val kernel =
        new ScalatraServlet with BigtopRoutes {
          get(Calculator)
        }

      kernel.handle(req, res)

      res.buffer.toString
    }
    
    /** Invokes a route's apply() method and returns the accumulated response content. */
    def testRouteApply(fn : => Any): Any = {
      val req = new TestRequest("", "")
      val res = new TestResponse

      val kernel =
        new ScalatraServlet with BigtopRoutes {
          override protected val _request  = new DynamicVariable[HttpServletRequest](req)
          override protected val _response = new DynamicVariable[HttpServletResponse](res)
          get(Calculator)
        }
      
      _kernel.withValue(Some(kernel)) {
        fn
      }
    }
    
    /** Invokes a route's apply() method and returns the accumulated response content. */
    def testWithRequest[T](req: TestRequest)(fn : => T): T = {
      val res = new TestResponse

      val kernel =
        new ScalatraServlet with BigtopRoutes {
          override protected val _request  = new DynamicVariable[HttpServletRequest](req)
          override protected val _response = new DynamicVariable[HttpServletResponse](res)
          get(Calculator)
        }
      
      _kernel.withValue(Some(kernel)) {
        fn
      }
    }
    
  }
  
  "site applies to the correct route" in {
    Calculator.testTopLevel(TestRequest("/add/1/to/2"))         mustEqual "1 + 2 = 3"
    Calculator.testTopLevel(TestRequest("/multiply/3/by/4"))    mustEqual "3 * 4 = 12"
    Calculator.testTopLevel(TestRequest("/square/5"))           mustEqual "5 * 5 = 25"
    Calculator.testTopLevel(TestRequest("/repeat/abc/2/times")) mustEqual "abc * 2 = abcabc"
    Calculator.testTopLevel(TestRequest("/append/abc/def/ghi")) mustEqual "append(List(abc, def, ghi)) = abcdefghi"
  }

  "site applies to the correct route when servlet and context paths are non-empty" in {
    Calculator.testTopLevel(TestRequest("/add/1/to/2",         "/servlet", "/context")) mustEqual "1 + 2 = 3"
    Calculator.testTopLevel(TestRequest("/multiply/3/by/4",    "/servlet", "/context")) mustEqual "3 * 4 = 12"
    Calculator.testTopLevel(TestRequest("/square/5",           "/servlet", "/context")) mustEqual "5 * 5 = 25"
    Calculator.testTopLevel(TestRequest("/repeat/abc/2/times", "/servlet", "/context")) mustEqual "abc * 2 = abcabc"
    Calculator.testTopLevel(TestRequest("/append/abc/def/ghi", "/servlet", "/context")) mustEqual "append(List(abc, def, ghi)) = abcdefghi"
  }

  "routes can be invoked directly" in {
    Calculator.testRouteApply(Calculator.add(1, 2))                         mustEqual "1 + 2 = 3"
    Calculator.testRouteApply(Calculator.multiply(3, 4))                    mustEqual "3 * 4 = 12"
    Calculator.testRouteApply(Calculator.square(5))                         mustEqual "5 * 5 = 25"
    Calculator.testRouteApply(Calculator.repeat("abc", 2))                  mustEqual "abc * 2 = abcabc"
    Calculator.testRouteApply(Calculator.append(List("abc", "def", "ghi"))) mustEqual "append(List(abc, def, ghi)) = abcdefghi"
  }
  
  "routes produce the correct urls" in {
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.add.url(1, 2))                         mustEqual Url("/add/1/to/2")
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.multiply.url(3, 4))                    mustEqual Url("/multiply/3/by/4")
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.square.url(5))                         mustEqual Url("/square/5")
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.repeat.url("abc", 2))                  mustEqual Url("/repeat/abc/2/times")
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.append.url(List("abc", "def", "ghi"))) mustEqual Url("/append/abc/def/ghi")
  }
  
  "routes produce the correct urls when servlet and context paths are non-empty" in {
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.add.url(1, 2))                         mustEqual Url("/context/servlet/add/1/to/2")
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.multiply.url(3, 4))                    mustEqual Url("/context/servlet/multiply/3/by/4")
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.square.url(5))                         mustEqual Url("/context/servlet/square/5")
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.repeat.url("abc", 2))                  mustEqual Url("/context/servlet/repeat/abc/2/times")
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.append.url(List("abc", "def", "ghi"))) mustEqual Url("/context/servlet/append/abc/def/ghi")
  }
  
  "routes produce the correct paths" in {
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.add.path(1, 2))                         mustEqual "/add/1/to/2"
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.multiply.path(3, 4))                    mustEqual "/multiply/3/by/4"
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.square.path(5))                         mustEqual "/square/5"
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.repeat.path("abc", 2))                  mustEqual "/repeat/abc/2/times"
    Calculator.testWithRequest(TestRequest("", ""))(Calculator.append.path(List("abc", "def", "ghi"))) mustEqual "/append/abc/def/ghi"
  }
  
  "routes produce the correct paths when servlet and context paths are non-empty" in {
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.add.path(1, 2))                         mustEqual "/context/servlet/add/1/to/2"
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.multiply.path(3, 4))                    mustEqual "/context/servlet/multiply/3/by/4"
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.square.path(5))                         mustEqual "/context/servlet/square/5"
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.repeat.path("abc", 2))                  mustEqual "/context/servlet/repeat/abc/2/times"
    Calculator.testWithRequest(TestRequest("", "/servlet", "/context"))(Calculator.append.path(List("abc", "def", "ghi"))) mustEqual "/context/servlet/append/abc/def/ghi"
  }
  
}
