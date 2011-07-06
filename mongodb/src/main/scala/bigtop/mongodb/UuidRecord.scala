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

import java.util.UUID

import net.liftweb.record._
import net.liftweb.record.field._

import com.foursquare.rogue._
import com.foursquare.rogue.Rogue._

/** Mix this into an IdRecord to add a UUID field that's separate from Mongo's ID. */
trait UuidRecord[T <: UuidRecord[T]] extends IdRecord[T] {
  self: T =>
  
  val uuid = new StringField(this, 36, genUuid)

  def genUuid = UUID.randomUUID.toString
  
}

/** Mix this into an IdRecordMeta to provide UuidRecord-related queries. */
trait UuidRecordMeta[T <: UuidRecord[T]] extends IdRecordMeta[T] {
  self: T =>
  
  def byUuid(uuid: String): Query[T] =
    this.where(_.uuid eqs uuid)
  
}
