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
package record

import net.liftweb.record._
import net.liftweb.record.field._

import org.scalatest._

// Tests ----------------------------------------

// Removed for now - see comments in EqualsHelper.scala for reasons:
// 
// class EqualsHelperSuite extends FunSuite with Assertions {
//   
//   test("equals, dataEquals") {
//     val p1 = User.createRecord.id(1).name("abc")
//     val p2 = User.createRecord.id(1).name("abc")
// 
//     assert(p1 === p2)
//     assert(p1 dataEquals p2)
//     
//     p2.id(2)
//     
//     assert(p1 != p2)
//     assert(p1 dataEquals p2)
//   }
//  
// }
