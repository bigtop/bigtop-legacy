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

package bigtop.routes.core

/**
 * A URL path pattern.
 */
sealed trait Path extends Bidi {
  
  type Outer = List[String]
  type Inner <: HList

}

case class PLiteral[T <: Path](val head: String, val tail: T) extends Path {
  
  type Inner = tail.Inner
  
  override def canDecode(path: List[String]): Boolean =
    !path.isEmpty && path.head == head && tail.canDecode(path.tail)
  
  def decode(path: List[String]): Option[Inner] =
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

  override def canDecode(path: List[String]): Boolean =
    !path.isEmpty && head.canDecode(path.head) && tail.canDecode(path.tail)
  
  def decode(path: List[String]): Option[Inner] =
    for {
      h <- head.decode(path.head)
      t <- tail.decode(path.tail)
    } yield HCons(h, t)
  
  def encode(args: Inner): List[String] =
    head.encode(args.head) :: tail.encode(args.tail)
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

sealed abstract class PNil extends Path {
  
  type Inner = HNil
  
  override def canDecode(path: List[String]): Boolean =
    path.isEmpty
  
  def decode(path: List[String]): Option[Inner] =
    if(path.isEmpty) Some(HNil) else None
  
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
  
  override def canDecode(path: List[String]): Boolean =
    true
  
  def decode(path: List[String]): Option[Inner] =
    Some(path :: HNil)

  def encode(args: Inner): List[String] =
    args.head
  
  def :/:(arg: String) =
    PLiteral(arg, this)
  
  def :/:[T](arg: Arg[T]) =
    PMatch(arg, this)
  
}

case object PAny extends PAny
