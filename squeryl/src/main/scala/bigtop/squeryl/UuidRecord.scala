package bigtop
package squeryl

import java.util.UUID

import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.annotations.Column

/** Mix this into an IdRecord to add a UUID field. */
trait UuidRecord[T <: UuidRecord[T]] extends IdRecord[T] {
  self: T =>
  
  val uuid = new StringField(this, 36, genUuid)

  def genUuid = UUID.randomUUID.toString
  
}

/** Mix this into an IdRecordMeta to provide UuidRecord-related queries. */
trait UuidRecordMeta[T <: UuidRecord[T]] extends IdRecordMeta[T] {
  self: T =>
  
  def byUuid(uuid: String): Query[T] =
    table.where(_.uuid === uuid)
  
}
