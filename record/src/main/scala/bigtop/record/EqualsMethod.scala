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
package record

import net.liftweb.record._

// /**
//  * Default equals methods for subclasses of Record:
//  *  - equals - compares all fields in the record;
//  *  - dataEquals - compares all fields except the primary key.
//  * 
//  * The methods compare the values of all fields except:
//  *  - PasswordTypedFields;
//  *  - fields marked with the IgnoreInEquals trait.
//  */
// [DJG] This seems to cause problems in the mongodb implementation,
// so I've copied the code out into bigtop.{squeryl,mongodb}.IdRecord for now.
//
// trait EqualsHelper[T <: EqualsHelper[T]] extends Record[T] {
//   self: T =>
//   
//   def idFieldForEquals: Field[_, T]
//   
//   lazy val comparableFields = 
//     allFields.filter(field => !field.isInstanceOf[PasswordField[_]] && 
//                               !field.isInstanceOf[IgnoreInEquals])
//   
//   lazy val comparableDataFields = 
//     comparableFields.filter(_ != idFieldForEquals)
//   
//   def fieldsEqual(fieldsOf: (T) => List[Field[_, T]])(that: T): Boolean =
//     fieldsOf(this).zip(fieldsOf(that)).foldLeft(true) {
//       (accum, zipped) => accum && (zipped._1.get == zipped._2.get)
//     }
//   
//   override def equals(other: Any) =
//     other match {
//       case that: T => fieldsEqual(_.comparableFields)(that)
//       case _ => false
//     }
//   
//   def dataEquals(that: T) = 
//     fieldsEqual(_.comparableDataFields)(that)
// 
// }
