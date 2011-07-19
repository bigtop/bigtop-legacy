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
package mongodb

import net.liftweb.common._
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.util._

import bigtop.record._

// Database -------------------------------------

object TestDb extends DbHelper {
  
  lazy val schema = TestSchema

  val database = "bigtopmongodbtestdb"

}

// Schema ---------------------------------------

object TestSchema extends DbSchema

// Entities -------------------------------------

class User private() extends IdRecord[User] with BaseUser[User] {
  def meta = User
  def schema = TestSchema
  
  val superuser = new BooleanField(this)
  
  override def canChangeIdentity = superuser.is
}

object User extends User with IdRecordMeta[User] with BaseUserMeta[User]

class Project private() extends IdRecord[Project] with UuidRecord[Project] {
  def meta = Project
  def schema = TestSchema
  
  val ignore = new StringField(this, 32) with IgnoreInEquals
}

object Project extends Project with IdRecordMeta[Project] with UuidRecordMeta[Project]
