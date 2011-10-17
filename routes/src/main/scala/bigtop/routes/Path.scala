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

/**
 * This Wrapper is here purely to please the type inference engine.
 *
 * The Path.decode() method should ideally return a Result,
 * but this can cause cyclic type inference errors.
 *
 * As a workaround, we wrap each return value of Path.decode()
 * in an instance of this marker class. This silences the errors
 * and allows us to go about our business.
 */ 
case class Wrapper[+T](value: T)

/**
 * A URL path pattern.
 */
sealed trait Path {
  
  type Inner <: HList

  def canDecode(path: List[String]): Boolean
  
  def decode(path: List[String]): Wrapper[Inner]
  
  def encode(args: Inner): List[String]
  
}

case class PLiteral[T <: Path](val head: String, val tail: T) extends Path {
  
  type Inner = tail.Inner
  
  def canDecode(path: List[String]): Boolean =
    !path.isEmpty && path.head == head && tail.canDecode(path.tail)
  
  def decode(path: List[String]): Wrapper[Inner] =
    tail.decode(path.tail)
    
  def encode(args: Inner): List[String] =
    head :: tail.encode(args)
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

case class PMatch[H, T <: Path](val head: Arg[H], val tail: T) extends Path {
  
  type Inner = HCons[H, tail.Inner]

  def canDecode(path: List[String]): Boolean =
    !path.isEmpty && head.canDecode(path.head) && tail.canDecode(path.tail)
  
  def decode(path: List[String]): Wrapper[Inner] =
    Wrapper(HCons(head.decode(path.head), tail.decode(path.tail).value))
  
  def encode(args: Inner): List[String] =
    head.encode(args.head) :: tail.encode(args.tail)
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

sealed abstract class PNil extends Path {
  
  type Inner = HNil
  
  def canDecode(path: List[String]): Boolean =
    path.isEmpty
  
  def decode(path: List[String]): Wrapper[Inner] =
    Wrapper(HNil)
  
  def encode(args: Inner): List[String] =
    Nil
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

case object PNil extends PNil

sealed abstract class PAny extends Path {
  
  type Inner = HCons[List[String], HNil]
  
  def canDecode(path: List[String]): Boolean =
    true
  
  def decode(path: List[String]): Wrapper[Inner] =
    Wrapper(path :: HNil)

  def encode(args: Inner): List[String] =
    args.head
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

case object PAny extends PAny
