package bigtop
package mongodb

import com.mongodb._
import net.liftweb.common._

/** All IdRecords are registered with a singleton Schema object, providing drop/recreate functionality for the whole database. */
trait DbSchema {

  // Tables need to be stored in reverse-dependency order:
  var records = List[IdRecordMeta[_]]()

  // Register new tables in our list:
  private[mongodb] def += (meta: IdRecordMeta[_]): Unit =
    records = meta :: records
  
  // Utility methods ----------------------------
  
  def dropAndRecreateDatabase: Unit = { 
    records.foreach(_.bulkDelete_!!(QueryBuilder.start.get))
  }
  
}
