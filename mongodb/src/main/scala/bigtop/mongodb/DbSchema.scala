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
