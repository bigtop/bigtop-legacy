package bigtop
package squeryl

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.db._
import net.liftweb.http.S
import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Session
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.adapters.PostgreSqlAdapter

/** Create a singleton DbHelper to provide easy command-line access to your DB. */
trait DbHelper {
  
  var initialised = false
  
  val schema: DbSchema
  
  val driver = "org.postgresql.Driver"
  val host = "localhost"
  val database: String
  val username: Box[String]
  val password: Box[String]
  
  def url = "jdbc:postgresql://" + host + "/" + database

  val adapter: DatabaseAdapter = new PostgreSqlAdapter
  
  var withDb = DB.buildLoanWrapper
  
  def init: Unit = {
    if(!initialised) {
      val manager = new StandardDBVendor(driver, url, username, password)
      
      DB.defineConnectionManager(DefaultConnectionIdentifier, manager)
    
      SquerylRecord.init(() => adapter)

      S.addAround(withDb)
      
      initialised = true
    }
  }
  
  def dropAndRecreateDatabase: Unit = {
    init
    withDb(schema.dropAndRecreateDatabase)
  }
  
  def dropAndRecreateTestData: Unit =
    dropAndRecreateDatabase

}
