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
  
  test("equals") {
    assert(Project.createRecord.uuid("abc") === Project.createRecord.uuid("abc"))
    assert(Project.createRecord.uuid("abc") != Project.createRecord.uuid("bcd"))
    assert(Project.createRecord.uuid("abc").ignore("a") === Project.createRecord.uuid("abc").ignore("b"))
  }

}
