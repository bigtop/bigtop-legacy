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
