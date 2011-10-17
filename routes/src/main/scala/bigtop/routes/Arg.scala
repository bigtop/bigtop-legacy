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
package routes

trait Arg[X] {
  
  type Outer = String
  type Inner = X

  def canDecode(a: Outer): Boolean
  
  def decode(a: Outer): Inner
  
  def encode(b: Inner): Outer
  
}

case object IntArg extends Arg[Int] {
  
  def canDecode(in: String) =
    try {
      in.toInt
      true
    } catch {
      case exn: NumberFormatException => false
    }
    
  def decode(in: String) =
    in.toInt

  def encode(in: Int) =
    in.toString
  
}

case object StringArg extends Arg[String] {
  
  def canDecode(in: String) =
    true
  
  def encode(in: String) =
    in
  
  def decode(in: String) =
    in
  
}
