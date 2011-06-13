package bigtop
package mongodb

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.db._
import net.liftweb.mongodb._
import net.liftweb.http.S

/** Create a singleton DbHelper to provide easy command-line access to your DB. */
trait DbHelper {
  
  var initialised = false
  
  val schema: DbSchema
  
  val database: String
  
  def init: Unit = {
    if(!initialised) {
      MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost(), database))
      
      initialised = true
    }
  }
  
  def dropAndRecreateDatabase: Unit = {
    init
    schema.dropAndRecreateDatabase
  }
  
  def dropAndRecreateTestData: Unit =
    dropAndRecreateDatabase

}
