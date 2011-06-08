package bigtop
package squeryl

import net.liftweb.common._
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util._

import org.squeryl.adapters.H2Adapter

// Database -------------------------------------

object TestDb extends DbHelper {
  
  lazy val schema = TestSchema

  override val driver = "org.h2.Driver"

  val database = "squeryltestdb"
  val username = Full("")
  val password = Full("")

  override def url = "jdbc:h2:mem:" + database
  
  override val adapter = new H2Adapter
  
}

// Schema ---------------------------------------

object TestSchema extends DbSchema {
  val users = table[User]
  val projects = table[Project]
}

// Entities -------------------------------------

class User private() extends IdRecord[User] with BaseUser[User] {
  def meta = User
  def table = TestSchema.users
}

object User extends User with IdRecordMeta[User] with BaseUserMeta[User]

class Project private() extends IdRecord[Project] with UuidRecord[Project] {
  def meta = Project
  def table = TestSchema.projects
  
  val ignore = new StringField(this, 32) with IgnoreInEquals
}

object Project extends Project with IdRecordMeta[Project] with UuidRecordMeta[Project]
