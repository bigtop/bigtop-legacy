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
  
  def self: This
  
  def site: Site
  
  var controller: Option[HListFunction[Result, Box[LiftResponse]]] = None
  
  final def decode(path: List[String]): Option[Result] =
    decodeInternal(path.reverse)
  
  def decodeInternal(path: List[String]): Option[Result]
  
  final def encode(args: Result): List[String] =
    encodeInternal(args).reverse
  
  def encodeInternal(args: Result): List[String]
  
  def url(args: Result): String =
    encode(args).mkString("/", "/", "")

  def apply(in: Req): Box[LiftResponse] =
    controller match {
      case Some(fn) => decode(in.path.partPath).map(fn(_)).getOrElse(Empty)
      case None     => Empty
    }

  def apply(in: Result): Box[LiftResponse] =
    controller match {
      case Some(fn) => fn(in)
      case None     => Empty
    }

  def >>(fn: HListFunction[Result, Box[LiftResponse]]): this.type = {
    controller = Some(fn)
    site.add(this)
    this
  }
}

/**
 * A PList matching a particular segment of a URL. There are two
 * subtypes: PMatch matches a URL argument, and PLiteral matches
 * a URL segment from which we don't want to extract an argument.
 */
abstract sealed class PCons[Hd, Tl <: Path](head: Arg[Hd], tail: Tl) extends Path

/**
 * A PCons cell representing a path fragment from which we're not
 * interested in capturing as an argument.
 */
case class PLiteral[Tl <: Path](head: LiteralArg, tail: Tl) extends PCons[Unit, Tl](head, tail) {
  type This = PLiteral[Tl]
  type Head = Unit
  type Tail = Tl
  type Result = tail.Result

  def self = this
  
  def site = tail.site

  def decodeInternal(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        if (head.decode(x).isDefined) tail.decodeInternal(xs) else None
      case Nil => None
    }
  
  def encodeInternal(args: Result): List[String] =
    head.encode(()) :: tail.encodeInternal(args)
}

/**
 * A PCons cell from which we want to capture an argument. The type of the
 * argument and the process of encoding/decoding it in the path are represented
 * by the MatchArg constructor argument.
 */
case class PMatch[Hd, Tl <: Path](head: MatchArg[Hd], tail: Tl) 
    extends PCons[Hd, Tl](head, tail) {
  type This = PMatch[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, tail.Result]
  
  def self = this

  def site = tail.site

  def decodeInternal(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        head.decode(x).flatMap{ v => 
          tail.decodeInternal(xs) match {
            case None     => None
            case Some(vs) => Some(HCons(v, vs))
          }
        }
      case Nil => None
    }
  
  def encodeInternal(args: Result): List[String] =
    head.encode(args.head) :: tail.encodeInternal(args.tail)
}

class PNil(val site: Site) extends Path {
  type This = PNil
  type Head = Nothing
  type Tail = PNil
  type Result = HNil

  def self = this

  def decodeInternal(path: List[String]): Option[Result] =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }
  
  def encodeInternal(args: Result): List[String] =
    Nil
  
  def /(v: LiteralArg): PLiteral[This] with Path0 =
    new PLiteral[This](v, self) with Path0 {}
  
  def /[V](v: MatchArg[V]): PMatch[V, This] with Path1[V] =
    new PMatch[V, This](v, self) with Path1[V] {}
  
  def url: String =
    url(HNil)
  
  def apply(): Box[LiftResponse] =
    apply(HNil)
}

trait Path0 extends Path {
  def /(v: LiteralArg): PLiteral[This] with Path0 =
    new PLiteral[This](v, self) with Path0 {}
  
  def /[V](v: MatchArg[V]): PMatch[V, This] with Path1[V] =
    new PMatch[V, This](v, self) with Path1[V] {}

  val mkArgs: Result =
    HNil.asInstanceOf[Result]
  
  def url: String =
    url(mkArgs)
  
  def apply(): Box[LiftResponse] =
    apply(mkArgs)
}

trait Path1[A] extends Path {
  def /(v: LiteralArg): PLiteral[This] with Path1[A] =
    new PLiteral[This](v, self) with Path1[A] {}
  
  def /[V](v: MatchArg[V]): PMatch[V, This] with Path2[A, V] =
    new PMatch[V, This](v, self) with Path2[A, V] {}
  
  def mkArgs(a: A): Result =
    HCons(a, HNil).asInstanceOf[Result]
  
  def url(a: A): String =
    url(mkArgs(a))

  def apply(a: A): Box[LiftResponse] =
    apply(mkArgs(a))
}

trait Path2[A, B] extends Path {
  def /(v: LiteralArg): PLiteral[This] with Path2[A, B] =
    new PLiteral[This](v, self) with Path2[A, B] {}
  
  def /[V](v: MatchArg[V]): PMatch[V, This] with Path3[A, B, V] =
    new PMatch[V, This](v, self) with Path3[A, B, V] {}
  
  def mkArgs(a: A, b: B): Result =
    HCons(b, HCons(a, HNil)).asInstanceOf[Result]
  
  def url(a: A, b: B): String =
    url(mkArgs(a, b))

  def apply(a: A, b: B): Box[LiftResponse] =
    apply(mkArgs(a, b))
}

trait Path3[A, B, C] extends Path {
  def /(v: LiteralArg): PLiteral[This] with Path3[A, B, C] =
    new PLiteral[This](v, self) with Path3[A, B, C] {}
  
  def /[V](v: MatchArg[V]): PMatch[V, This] with Path4[A, B, C, V] =
    new PMatch[V, This](v, self) with Path4[A, B, C, V] {}
  
  def mkArgs(a: A, b: B, c: C): Result =
    HCons(c, HCons(b, HCons(a, HNil))).asInstanceOf[Result]
  
  def url(a: A, b: B, c: C): String =
    url(mkArgs(a, b, c))

  def apply(a: A, b: B, c: C): Box[LiftResponse] =
    apply(mkArgs(a, b, c))
}

trait Path4[A, B, C, D] extends Path {
  def /(v: LiteralArg): PLiteral[This] with Path4[A, B, C, D] =
    new PLiteral[This](v, self) with Path4[A, B, C, D] {}
  
  // def /[V](v: MatchArg[V]): PMatch[V, This] with Path5 =
  //   new PMatch[V, This](v, self) with Path5 {}
  
  def mkArgs(a: A, b: B, c: C, d: D): Result =
    HCons(d, HCons(c, HCons(b, HCons(a, HNil)))).asInstanceOf[Result]
  
  def url(a: A, b: B, c: C, d: D): String =
    url(mkArgs(a, b, c, d))

  def apply(a: A, b: B, c: C, d: D): Box[LiftResponse] =
    apply(mkArgs(a, b, c, d))
}
