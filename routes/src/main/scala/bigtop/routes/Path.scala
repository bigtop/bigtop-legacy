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

import net.liftweb.common._
import net.liftweb.http._

/**
* URL path patterns are represented by Path objects,
* of which there are PCons and PNil subtypes.
* 
* PConses are parameterised by the type of the argument
* we expect to capture in that segment of the path. Constant
* segments are given type Unit.
* 
* Once we have matched the complete path, we are able to extract a
* heterogeneous list (HList) of argument values. We convert this to a
* tuple or argument list as appropriate.
*
* There are four type members in a Path:
*  - This - the type of the whole path pattern;
*  - Head - the type of the argument being captured in this segment of the path
*           (or Unit if we're not interested in this path segment);
*  - Tail - the type of the parent path pattern;
*  - Result - the type of the argument list being extracted from the path
*             (an HList of the same or lesser length than the path).
*/
sealed trait Path {
  type This <: Path
  type Head
  type Tail <: Path
  type Result <: HList // The result type if this pattern matches

  def decode(path: List[String]): Option[Result]
  
  def encode(args: Result): List[String]
  
  def >>(fn: HListFunction[Result, Box[LiftResponse]]): Route[Result] =
    new Route[Result] {
      def apply(in: Req): Box[LiftResponse] =
        decode(in.path.partPath).map(fn(_)).getOrElse(Empty)

      def url(args: Result): String = encode(args).mkString("/", "/", "")
    }
}

/**
* A PList matching a particular segment of a URL. There are two
* subtypes: PMatch matches a URL argument, and PLiteral matches
* a URL segment from which we don't want to extract an argument.
*/
abstract sealed class PCons[Hd, Tl <: Path](head: Arg[Hd], tail: Tl) extends Path {
  def /:(v: LiteralArg): PLiteral[This]
  
  def /:[V](v: MatchArg[V]): PMatch[V, This]
}

/**
* A PCons cell representing a path fragment from which we're not
* interested in capturing as an argument.
*/
final case class PLiteral[Tl <: Path](head: LiteralArg, tail: Tl) 
           extends PCons[Unit, Tl](head, tail) {
  type This = PLiteral[Tl]
  type Head = Unit
  type Tail = Tl
  type Result = tail.Result
  
  def /:[V](v: MatchArg[V]): PMatch[V, This] =
    PMatch[V, This](v, this)
  
  def /:(v: LiteralArg): PLiteral[This] =
    PLiteral[This](v, this)

  def decode(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        if (head.decode(x).isDefined) tail.decode(xs) else None
      case Nil => None
    }
  
  def encode(args: Result): List[String] =
    head.encode(()) :: tail.encode(args)
}

/**
* A PCons cell from which we want to capture an argument. The type of the
* argument and the process of encoding/decoding it in the path are represented
* by the MatchArg constructor argument.
*/
final case class PMatch[Hd, Tl <: Path](head: MatchArg[Hd], tail: Tl) 
           extends PCons[Hd, Tl](head, tail) {
  type This = PMatch[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, tail.Result]

  def /:(v: LiteralArg): PLiteral[This] =
    PLiteral[This](v, this)
  
  def /:[V](v: MatchArg[V]): PMatch[V, This] =
    PMatch[V, This](v, this)

  def decode(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        head.decode(x).flatMap{ v => 
          tail.decode(xs) match {
            case None     => None
            case Some(vs) => Some(HCons(v, vs))
          }
        }
      case Nil => None
    }
  
  def encode(args: Result): List[String] =
    head.encode(args.head) :: tail.encode(args.tail)
}

class PNil extends Path {
  type This = PNil
  type Head = Nothing
  type Tail = PNil
  type Result = HNil

  def /:(v: LiteralArg): PLiteral[This] =
    PLiteral[This](v, this)
  
  def /:[V](v: MatchArg[V]): PMatch[V, This] =
    PMatch[V, This](v, this)

  def decode(path: List[String]): Option[Result] =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }
  
  def encode(args: Result): List[String] =
    Nil
}

case object PNil extends PNil
