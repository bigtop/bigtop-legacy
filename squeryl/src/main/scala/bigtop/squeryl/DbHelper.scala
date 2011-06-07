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

  val host = "localhost"
  val database: String
  val username: Box[String]
  val password: Box[String]

  var withDb = DB.buildLoanWrapper
  
  def init: Unit = {
    if(!initialised) {
      val driver = "org.postgresql.Driver"
    
      val url = "jdbc:postgresql://" + host + "/" + database
    
      val manager = new StandardDBVendor(driver, url, username, password)
      val adapter = new PostgreSqlAdapter
    
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
