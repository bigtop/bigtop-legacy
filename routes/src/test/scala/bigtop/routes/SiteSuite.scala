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
  
  val emptyOp: () => Box[LiftResponse] = () => Empty
  val intOp: Int => Box[LiftResponse] = i => Empty

  object MySite extends Site {
    val listContacts = add("contacts" /: PNil >> emptyOp)

    val newContact = add(("contacts" /: "new" /: PNil) >> emptyOp)

    val viewContact = add(("contacts" /: IntArg /: PNil) >> intOp)

    val editContact = add(("contacts" /: IntArg /: "edit" /: PNil) >> intOp)

    val deleteContact = add(("contacts" /: IntArg /: "delete" /: PNil) >> intOp)
  }

  test("viewContact url") {
    expect("/contacts/123")(MySite.viewContact.url(Tuple1(123)))
  }

}
