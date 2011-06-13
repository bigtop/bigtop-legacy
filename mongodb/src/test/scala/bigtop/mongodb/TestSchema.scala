package bigtop
package mongodb

import net.liftweb.common._
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.util._

// Database -------------------------------------

object TestDb extends DbHelper {
  
  lazy val schema = TestSchema

  val database = "mongodbtestdb"

}

// Schema ---------------------------------------

object TestSchema extends DbSchema

// Entities -------------------------------------

class User private() extends IdRecord[User] with BaseUser[User] {
  def meta = User
  def schema = TestSchema
}

object User extends User with IdRecordMeta[User] with BaseUserMeta[User]

class Project private() extends IdRecord[Project] with UuidRecord[Project] {
  def meta = Project
  def schema = TestSchema
  
  val ignore = new StringField(this, 32) with IgnoreInEquals
}

object Project extends Project with IdRecordMeta[Project] with UuidRecordMeta[Project]
