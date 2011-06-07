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
