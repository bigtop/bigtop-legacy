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
package routes

import net.liftweb.http.Req
import org.specs._

class LiftSiteSpec extends Specification with LiftHttp {
  
  object Calculator extends LiftSite {
  
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
    Calculator(TestRequest("/add/1/to/2"))         must matchResponse(TestResponse("1 + 2 = 3"))
    Calculator(TestRequest("/multiply/3/by/4"))    must matchResponse(TestResponse("3 * 4 = 12"))
    Calculator(TestRequest("/square/5"))           must matchResponse(TestResponse("5 * 5 = 25"))
    Calculator(TestRequest("/repeat/abc/2/times")) must matchResponse(TestResponse("abc * 2 = abcabc"))
    Calculator(TestRequest("/append/abc/def/ghi")) must matchResponse(TestResponse("append(List(abc, def, ghi)) = abcdefghi"))
  }

  "routes can be invoked directly" in {
    import HListOps._
    
    Calculator.add(1, 2)                         must matchResponse(TestResponse("1 + 2 = 3"))
    Calculator.multiply(3, 4)                    must matchResponse(TestResponse("3 * 4 = 12"))
    Calculator.square(5)                         must matchResponse(TestResponse("5 * 5 = 25"))
    Calculator.repeat("abc", 2)                  must matchResponse(TestResponse("abc * 2 = abcabc"))
    Calculator.append(List("abc", "def", "ghi")) must matchResponse(TestResponse("append(List(abc, def, ghi)) = abcdefghi"))
  }
  
  "routes produce the correct urls" in {
    import HListOps._
    
    Calculator.add.url(1, 2)                         mustEqual "/add/1/to/2"
    Calculator.multiply.url(3, 4)                    mustEqual "/multiply/3/by/4"
    Calculator.square.url(5)                         mustEqual "/square/5"
    Calculator.repeat.url("abc", 2)                  mustEqual "/repeat/abc/2/times"
    Calculator.append.url(List("abc", "def", "ghi")) mustEqual "/append/abc/def/ghi"
  }

  
}
