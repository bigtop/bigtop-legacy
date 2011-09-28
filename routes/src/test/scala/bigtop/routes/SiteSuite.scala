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

import org.scalatest._

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.LiftResponse

import HListOps._

class SiteSuite extends FunSuite with Assertions {
  
  object MySite extends Site {
    val home   = root >> (handleEmpty _)
    val show   = root / "show" >> (handleEmpty _)
    val negate = root / "negate" / IntArg >> (handleInt _)
    val add    = root / "add" / IntArg / "and" / IntArg >> (handleInts _)
    val concat = root / "concat" / StringArg / "and" / IntArg >> (handleStringAndInt _)

    def handleEmpty: Box[LiftResponse] = Empty
    
    def handleInt(i: Int): Box[LiftResponse] = Empty
    def handleInts(a: Int, b: Int): Box[LiftResponse] = Empty
    def handleStringAndInt(a: String, b: Int): Box[LiftResponse] = Empty
  }

  test("site.paths") {
    val expected =
      List[Path](MySite.home,
                 MySite.show,
                 MySite.negate,
                 MySite.add,
                 MySite.concat)
    
    expect(expected)(MySite.paths)
  }
  
  test("path.url") {
    expect("/")(MySite.home.url)
    expect("/show")(MySite.show.url)
    expect("/negate/123")(MySite.negate.url(123))
    expect("/add/123/and/234")(MySite.add.url(123, 234))
    expect("/concat/123/and/234")(MySite.concat.url("123", 234))
  }

  test("path.apply") {
    expect(Empty)(MySite.home())
    expect(Empty)(MySite.show())
    expect(Empty)(MySite.negate(123))
    expect(Empty)(MySite.add(123, 234))
    expect(Empty)(MySite.concat("123", 234))
  }

}
