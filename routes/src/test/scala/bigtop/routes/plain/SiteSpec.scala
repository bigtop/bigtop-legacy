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

    def doAdd(a: Int, b: Int) =
      TestResponse("%s + %s = %s".format(a, b, a + b))
  
    def doMultiply(a: Int, b: Int) =
      TestResponse("%s * %s = %s".format(a, b, a * b))
  
    def doSquare(a: Int) =
      multiply(a, a)

    def doRepeat(a: String, b: Int) =
      TestResponse("%s * %s = %s".format(a, b, a * b))
  
    def doAppend(a: List[String]) =
      TestResponse("append(%s) = %s".format(a, a.mkString))
    
  }
  
  "site applies to the correct route" in {
    Calculator(TestRequest("/add/1/to/2"))         must beSome(TestResponse("1 + 2 = 3"))
    Calculator(TestRequest("/multiply/3/by/4"))    must beSome(TestResponse("3 * 4 = 12"))
    Calculator(TestRequest("/square/5"))           must beSome(TestResponse("5 * 5 = 25"))
    Calculator(TestRequest("/repeat/abc/2/times")) must beSome(TestResponse("abc * 2 = abcabc"))
    Calculator(TestRequest("/append/abc/def/ghi")) must beSome(TestResponse("append(List(abc, def, ghi)) = abcdefghi"))
  }

  "routes can be invoked directly" in {
    Calculator.add(1, 2)                         mustEqual TestResponse("1 + 2 = 3")
    Calculator.multiply(3, 4)                    mustEqual TestResponse("3 * 4 = 12")
    Calculator.square(5)                         mustEqual TestResponse("5 * 5 = 25")
    Calculator.repeat("abc", 2)                  mustEqual TestResponse("abc * 2 = abcabc")
    Calculator.append(List("abc", "def", "ghi")) mustEqual TestResponse("append(List(abc, def, ghi)) = abcdefghi")
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
