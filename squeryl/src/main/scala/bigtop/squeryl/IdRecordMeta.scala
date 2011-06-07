package bigtop
package squeryl

import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.annotations.Column

/** Mix this into a MetaRecord to provide extra IdRecord-oriented queries. */
trait IdRecordMeta[T <: IdRecord[T]] extends MetaRecord[T] {
  self: T =>
    
  def byId(id: Long): Query[T] = table.where(_.idField === id)
  
  def all: Query[T] = table.where(_ => true)
  
}
