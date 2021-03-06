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

import org.bson.types.ObjectId

import net.liftweb.mongodb.record._
import net.liftweb.record._
import net.liftweb.record.field.PasswordField

import com.foursquare.rogue._
import com.foursquare.rogue.Rogue._

import bigtop.record._

/** Mix this into a MongoRecord to get a primary key, and save, delete, and equals methods. */
trait IdRecord[T <: IdRecord[T]] extends MongoRecord[T] with MongoId[T] {
  self: T =>
  
  def schema: DbSchema
  
  def idString = id.toString
  
  def isPersisted: Boolean =
    meta.asInstanceOf[IdRecordMeta[T]].byId(id).count > 0
  
  def delete: T = {
    delete_!
    this
  }
  
  lazy val comparableFields = 
    allFields.filter(field => !field.isInstanceOf[net.liftweb.record.field.PasswordField[_]] && 
                              !field.isInstanceOf[IgnoreInEquals])
  
  lazy val comparableDataFields = 
    comparableFields.filter(_ != _id)
  
  def fieldsEqual(fieldsOf: (T) => List[Field[_, T]])(that: T): Boolean =
    fieldsOf(this).zip(fieldsOf(that)).foldLeft(true) {
      (accum, zipped) => accum && (zipped._1.get == zipped._2.get)
    }
  
  override def equals(other: Any) =
    other match {
      case that: T => fieldsEqual(_.comparableFields)(that)
      case _ => false
    }
  
  def dataEquals(that: T) = 
    fieldsEqual(_.comparableDataFields)(that)

}

/** Mix this into a MongoMetaRecord to provide extra IdRecord-oriented queries. */
trait IdRecordMeta[T <: IdRecord[T]] extends MongoMetaRecord[T] {
  self: T =>
    
  schema += this
  
  def all: Query[T]  =
    Rogue.metaRecordToQueryBuilder(this)
  
  def byId(id: ObjectId): Query[T] =
    all.where(_._id eqs id)
  
  def byId(id: String): Query[T] =
    byId(new ObjectId(id))
  
}
