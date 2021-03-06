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

import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.annotations.Column

import bigtop.record._

/** Mix this into a Record to get a primary key, and save, delete, and equals methods. */
trait IdRecord[T <: IdRecord[T]] extends Record[T] with KeyedRecord[Long] {
  self: T =>
  
  def table: Table[T]
  
  @Column(name="id")
  override val idField = new LongField(this)
  
  def save: T = table.insertOrUpdate(this)

  def delete: T = {
    table.delete(table.where(_.idField === this.idField.is))
    idField(0)
    this
  }
  
  lazy val comparableFields = 
    allFields.filter(field => !field.isInstanceOf[net.liftweb.record.field.PasswordField[_]] && 
                              !field.isInstanceOf[IgnoreInEquals])
  
  lazy val comparableDataFields = 
    comparableFields.filter(_ != idField)
  
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

/** Mix this into a MetaRecord to provide extra IdRecord-oriented queries. */
trait IdRecordMeta[T <: IdRecord[T]] extends MetaRecord[T] {
  self: T =>
    
  def byId(id: Long): Query[T] =
    table.where(_.idField === id)
  
  def all: Query[T] =
    table
  
}
