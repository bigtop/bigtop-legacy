package bigtop
package squeryl

import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl._

/** Extension of Squeryl's Schema providing easy drop-and-recreate functionality. */
trait DbSchema extends Schema {

  // Tables need to be stored in reverse-dependency order:
  var tables = List[Table[_]]()

  // Register new tables in our list:
  override def table[T]()(implicit manifestT: Manifest[T]): Table[T] = {
    val ans = super.table()(manifestT)
    tables = ans :: tables
    ans
  }
  
  // Register new tables in our list:
  override def table[T](name: String)(implicit manifestT: Manifest[T]): Table[T] = {
    val ans = super.table(name)(manifestT)
    tables = ans :: tables
    ans
  }
  
  // Register new tables in our list:
  override def table[T](name: String, prefix: String)(implicit manifestT: Manifest[T]): Table[T] = {
    val ans = super.table(name, prefix)(manifestT)
    tables = ans :: tables
    ans
  }
  
  // Utility methods ----------------------------
  
  def deleteAllData: Unit = tables.foreach(_.deleteWhere(_ => true))
  
  def dropAndRecreateDatabase: Unit = { drop; create }
  
  // Postgres-friendly naming -------------------
  
  override def tableNameFromClassName(name: String) =
    super.tableNameFromClassName(name).toLowerCase
  
  override def columnNameFromPropertyName(name: String) =
    super.columnNameFromPropertyName(name).toLowerCase

}
