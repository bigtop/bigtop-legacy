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
package squeryl

import net.liftweb.db._
import net.liftweb.squerylrecord.RecordTypeMode._

class IdRecordSuite extends BaseSuite {

  test("save") {
    // Create a struct without saving it to the database:
    val project1 = Project.createRecord.uuid("foo")
    
    // ID starts at 0, database starts empty:
    expect(0)(project1.idField.is)
    expect(None)(Project.byUuid("foo").headOption)

    // Save the struct:
    val project2 = project1.save
    
    // Return value of save is the exact same struct:
    assert(project1 eq project2)
    assert(project1.idField.is > 0)

    // Database contains the struct:
    expect(Some(project2))(Project.byUuid("foo").headOption)
  }

  test("delete") {
    // Create a struct and save it to the database:
    val project1 = Project.createRecord.uuid("foo").save
    
    // ID is assigned, struct is in database:
    assert(project1.idField.is > 0)
    expect(Some(project1))(Project.byUuid("foo").headOption)
    
    // Delete the struct:
    val project2 = project1.delete
    
    // Return value of delete is the exact same struct:
    assert(project1 eq project2)

    // ID set to 0, database ends up empty:
    expect(0)(project1.idField.is)
    expect(None)(Project.byUuid("foo").headOption)
  }
  
  test("byId") {
    val project1 = Project.createRecord.uuid("foo").save
    expect(Some(project1))(Project.byId(project1.id).headOption)
  }
  
  test("equals, dataEquals") {
    val p1 = Project.createRecord.uuid("abc")
    val p2 = Project.createRecord.uuid("abc")

    assert(p1 === p2)
    assert(p1 dataEquals p2)
    
    p1.save
    p2.save
    
    assert(p1 != p2)
    assert(p1 dataEquals p2)
    
    TestDb.dropAndRecreateDatabase
    
    val p3 = Project.createRecord.uuid("abc").save
    val p4 = Project.byUuid("abc").headOption.get
    
    assert(p3 === p4)
    assert(p3 dataEquals p4)
  }

}
