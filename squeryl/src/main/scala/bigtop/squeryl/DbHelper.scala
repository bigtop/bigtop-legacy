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

import java.sql.{DriverManager,Driver}
import com.mchange.v2.c3p0.ComboPooledDataSource

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.db._
import net.liftweb.http.S
import net.liftweb.util.LoanWrapper
import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.squerylrecord.RecordTypeMode._

import org.squeryl.Session
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.adapters.PostgreSqlAdapter

/** Create a singleton DbHelper to provide easy command-line access to your DB. */
trait DbHelper extends net.liftweb.common.Loggable {
  
  var initialised = false
  
  val driver = "org.postgresql.Driver"
  
  val host = "localhost"
  val database: String
  val username: Option[String]
  val password: Option[String]

  /**
   * Reference to the schema stored in this database.
   * *Must* be declared as lazily evaluated value (i.e. a method or a lazy val).
   * 
   * If you declare this field with a regular val, you get a PrimitiveTypeMode
   * vs RecordTypeMode exception at initialisation time.
   */
  def schema: DbSchema
  
  def url =
    "jdbc:postgresql://" + host + "/" + database
  
  lazy val pool = {
    val pool = new ComboPooledDataSource
    pool.setDriverClass(driver)
    pool.setJdbcUrl(url)
    pool.setUser(username.getOrElse(""))
    pool.setPassword(password.getOrElse(""))
    pool.setMinPoolSize(5)
    pool.setAcquireIncrement(5)
    pool.setMaxPoolSize(20)
	  pool
  }
  
  def createConnection =
    pool.getConnection
  
  val adapter: DatabaseAdapter =
    new PostgreSqlAdapter
  
  val withDb =
    new LoanWrapper {
      def apply[T](fn: => T): T = {
        try {
          logger.debug("bigtop.squeryl.DbHelper.withDb starting")
          inTransaction(fn)
        } finally {
          logger.debug("bigtop.squeryl.DbHelper.withDb finished")
        }
      }
    }
  
  def init: Unit = {
    if(!initialised) {
      try {
        logger.debug("bigtop.squeryl.DbHelper.init starting")
        SquerylRecord.initWithSquerylSession {
          val session = Session.create(createConnection, adapter)
          session
        }

        S.addAround(withDb)
      
        initialised = true
      } finally {
        logger.debug("bigtop.squeryl.DbHelper.init finished")
      }
    }
  }
  
  // Handy drop-and-recreate functionality ------
  
  def dropAndRecreateDatabase: Unit = {
    init
    withDb(schema.dropAndRecreateDatabase)
  }
  
  def dropAndRecreateTestData: Unit =
    dropAndRecreateDatabase

}
