package bigtop
package squeryl

import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Table
import org.squeryl.Query
import org.squeryl.annotations.Column

/** Mix this into a Field to prevent it being compared in IdRecord.equals() */
trait IgnoreInEquals

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
  
  override def equals(other: Any) = other match {
    case that: T =>
      this.fields.zip(that.fields).foldLeft(true) { (accum, zipped) =>
        accum && (zipped match {
          case (field1: PasswordField[_], field2: PasswordField[_]) => true
          case (field1: IgnoreInEquals, field2: IgnoreInEquals) => true
          case (field1, field2) => field1.get == field2.get
        })
      }

    case _ => false
  }
  
}
