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

class UuidRecordSuite extends BaseSuite {
  
  val uuidRegex = """^\w{8}-\w{4}-\w{4}-\w{4}-\w{12}$"""

  test("byUuid") {
    val project1 = Project.createRecord.uuid("foo").save
    expect(Some(project1))(Project.byUuid("foo").headOption)
  }
  
  test("genUuid") {
    val project = Project.createRecord
    
    val uuids = (0 to 4).map(_ => project.genUuid)

    for(uuid <- uuids) {
      assert(uuid.matches(uuidRegex))
    }

    for((uuid1, i) <- uuids.zipWithIndex; (uuid2, j) <- uuids.zipWithIndex) {
      if(i != j) {
        assert(uuid1 != uuid2)
      }
    }
  }

}
