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

import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.squerylrecord._

import bigtop.record.{
  PasswordField => VanillaPasswordField,
  OptionalPasswordField => VanillaOptionalPasswordField
}

class PasswordField[OwnerType <: Record[OwnerType]](rec: OwnerType)
  extends VanillaPasswordField[OwnerType](rec)
  with SquerylRecordField

class OptionalPasswordField[OwnerType <: Record[OwnerType]](rec: OwnerType)
  extends VanillaOptionalPasswordField[OwnerType](rec)
  with SquerylRecordField
