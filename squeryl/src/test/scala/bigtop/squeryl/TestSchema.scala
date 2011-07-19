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

import java.sql.DriverManager
import net.liftweb.common._
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.util._

import org.squeryl.adapters.H2Adapter

import bigtop.record._

// Database -------------------------------------

object TestDb extends DbHelper {

  lazy val schema = TestSchema
  
  override val driver = "org.h2.Driver"
  
  val database = "bigtoptestdb"
  val username = None
  val password = None
  
  override def url =
    "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1"
  
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
  
  val superuser = new BooleanField(this)
  
  override def canChangeIdentity = superuser.is
}

object User extends User with IdRecordMeta[User] with BaseUserMeta[User]

class Project private() extends IdRecord[Project] with UuidRecord[Project] {
  def meta = Project
  def table = TestSchema.projects
  
  val ignore = new StringField(this, 32) with IgnoreInEquals
}

object Project extends Project with IdRecordMeta[Project] with UuidRecordMeta[Project]
