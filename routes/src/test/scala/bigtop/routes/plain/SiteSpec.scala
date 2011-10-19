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

package bigtop.routes.plain

import bigtop.core.Url
import net.liftweb.http.Req
import org.specs._

class SiteSpec extends Specification with Http {
  
  object Calculator extends Site {
  
    // Routing table:

    val add      = ("add"      :/: IntArg    :/: "to"   :/: IntArg  :/: end) >> (doAdd _)
    val multiply = ("multiply" :/: IntArg    :/: "by"   :/: IntArg  :/: end) >> (doMultiply _)
    val square   = ("square"   :/: IntArg                           :/: end) >> (doSquare _)
    val repeat   = ("repeat"   :/: StringArg :/: IntArg :/: "times" :/: end) >> (doRepeat _)
    val append   = ("append"                                        :/: any) >> (doAppend _)

    // Implementation:

    def doAdd(a: Int, b: Int): Unit =
      response.getWriter.print("%s + %s = %s".format(a, b, a + b))
  
    def doMultiply(a: Int, b: Int): Unit =
      response.getWriter.print("%s * %s = %s".format(a, b, a * b))
    
    def doSquare(a: Int): Unit =
      multiply(a, a)

    def doRepeat(a: String, b: Int): Unit =
      response.getWriter.print("%s * %s = %s".format(a, b, a * b))
  
    def doAppend(a: List[String]): Unit =
      response.getWriter.print("append(%s) = %s".format(a, a.mkString))
    
    // Hooks for tests:
    
    /** Invokes the top-level apply() method and returns the accumulated response content. */
    def testTopLevel(req: TestRequest): String = {
      val res = new TestResponse
      _response.withValue(Some(res)) {
        apply(req, res)
      }
      res.buffer.toString
    }
    
    /** Invokes a route's apply() method and returns the accumulated response content. */
    def testRouteApply(fn : => Unit): String = {
      val res = new TestResponse
      _response.withValue(Some(res)) {
        fn
      }
      res.buffer.toString
    }
    
  }
  
  "site applies to the correct route" in {
    Calculator.testTopLevel(TestRequest("/add/1/to/2"))         mustEqual "1 + 2 = 3"
    Calculator.testTopLevel(TestRequest("/multiply/3/by/4"))    mustEqual "3 * 4 = 12"
    Calculator.testTopLevel(TestRequest("/square/5"))           mustEqual "5 * 5 = 25"
    Calculator.testTopLevel(TestRequest("/repeat/abc/2/times")) mustEqual "abc * 2 = abcabc"
    Calculator.testTopLevel(TestRequest("/append/abc/def/ghi")) mustEqual "append(List(abc, def, ghi)) = abcdefghi"
  }

  "routes can be invoked directly" in {
    Calculator.testRouteApply(Calculator.add(1, 2))                         mustEqual "1 + 2 = 3"
    Calculator.testRouteApply(Calculator.multiply(3, 4))                    mustEqual "3 * 4 = 12"
    Calculator.testRouteApply(Calculator.square(5))                         mustEqual "5 * 5 = 25"
    Calculator.testRouteApply(Calculator.repeat("abc", 2))                  mustEqual "abc * 2 = abcabc"
    Calculator.testRouteApply(Calculator.append(List("abc", "def", "ghi"))) mustEqual "append(List(abc, def, ghi)) = abcdefghi"
  }
  
  "routes produce the correct urls" in {
    Calculator.add.url(1, 2)                         mustEqual Url("/add/1/to/2")
    Calculator.multiply.url(3, 4)                    mustEqual Url("/multiply/3/by/4")
    Calculator.square.url(5)                         mustEqual Url("/square/5")
    Calculator.repeat.url("abc", 2)                  mustEqual Url("/repeat/abc/2/times")
    Calculator.append.url(List("abc", "def", "ghi")) mustEqual Url("/append/abc/def/ghi")
  }
  
  "routes produce the correct paths" in {
    Calculator.add.path(1, 2)                         mustEqual "/add/1/to/2"
    Calculator.multiply.path(3, 4)                    mustEqual "/multiply/3/by/4"
    Calculator.square.path(5)                         mustEqual "/square/5"
    Calculator.repeat.path("abc", 2)                  mustEqual "/repeat/abc/2/times"
    Calculator.append.path(List("abc", "def", "ghi")) mustEqual "/append/abc/def/ghi"
  }
  
}
