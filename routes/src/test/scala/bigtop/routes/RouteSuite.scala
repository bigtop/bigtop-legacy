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

class RouteSuite extends FunSuite with Assertions {

  import HListOps._
  import ArgOps._

  test("Route construction with varying implicits type checks") {
    val func = () => Empty
    PNil / "foo" / "bar" >> func
    PNil / "foo" / ConstArg("bar") >> func
    PNil / "foo" / "bar" >> func
  }

}
