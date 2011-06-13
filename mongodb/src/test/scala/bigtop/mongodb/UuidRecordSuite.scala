package bigtop
package mongodb

import net.liftweb.db._

class UuidRecordSuite extends BaseSuite {
  
  val uuidRegex = """^\w{8}-\w{4}-\w{4}-\w{4}-\w{12}$"""

  test("byUuid") {
    val project1 = Project.createRecord.uuid("foo").save
    expect(Some(project1))(Project.byUuid("foo").get)
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
