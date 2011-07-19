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
    val home = root >> (handleEmpty _)
    val listContacts  = root / "contacts" >> (handleEmpty _)
    val newContact = root / "contacts" / "new" >> (handleEmpty _)
    val viewContact = root / "contacts" / IntArg >> (handleInt _)
    val editContact = root / "contacts" / IntArg / "edit" >> (handleInt _)
    val deleteContact = root / "contacts" / IntArg / "delete" >> (handleInt _)

    def handleEmpty: Box[LiftResponse] = Empty
    
    def handleInt(i: Int): Box[LiftResponse] = Empty
  }

  test("site routes") {
    expect(MySite.home ::
           MySite.listContacts ::
           MySite.newContact ::
           MySite.viewContact ::
           MySite.editContact ::
           MySite.deleteContact ::
           Nil)(MySite.routes)
  }
  
  test("viewContact url") {
    expect("/contacts/123")(MySite.viewContact.url(Tuple1(123)))
  }

}
