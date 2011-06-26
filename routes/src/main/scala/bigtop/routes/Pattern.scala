package bigtop
package routes

case class PatternException(msg: String, value: Any) extends Exception(msg) {
  override def toString: String =
    msg + " " + value.toString
}

class UrlPattern[P <: Pattern](pList: P) {  
  def decode(path: List[String]): Option[P#Result] = pList.decode(path)
}

/**
* URL path patterns are represented by Pattern objects,
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
* There are four type members in a Pattern:
*  - This - the type of the whole path pattern;
*  - Head - the type of the argument being captured in this segment of the path
*           (or Unit if we're not interested in this path segment);
*  - Tail - the type of the parent path pattern;
*  - Result - the type of the argument list being extracted from the path
*             (an HList of the same or lesser length than the path).
*/
sealed trait Pattern {
  type This <: Pattern
  type Head
  type Tail <: Pattern
  type Result <: HList // The result type if this pattern matches

  def decode(path: List[String]): Option[Result]
  
  def encode(args: Result): List[String]
}

/**
* A PList matching a particular segment of a URL. There are two
* subtypes: PMatch matches a URL argument, and PLiteral matches
* a URL segment from which we don't want to extract an argument.
*/
abstract sealed class PCons[Hd, Tl <: Pattern](head: Arg[Hd], tail: Tl) extends Pattern {
  def /:(v: LiteralArg): PLiteral[This]
  
  def /:[V](v: MatchArg[V]): PMatch[V, This]
}

/**
* A PCons cell representing a path fragment from which we're not
* interested in capturing as an argument.
*/
final case class PLiteral[Tl <: Pattern](head: LiteralArg, tail: Tl) 
           extends PCons[Unit, Tl](head, tail) {
  type This = PLiteral[Tl]
  type Head = Unit
  type Tail = Tl
  type Result = Tl#Result
  
  def /:[V](v: MatchArg[V]): PMatch[V, This] = PMatch[V, This](v, this)
  
  def /:(v: LiteralArg): PLiteral[This] = PLiteral[This](v, this)

  def decode(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        if (head.decode(x).isDefined) tail.decode(xs) else None
      case Nil => None
    }
  
  def encode(args: Result): List[String] =
    head.encode(()) ::
    tail.encode(args.asInstanceOf[PLiteral.this.tail.Result])
}

/**
* A PCons cell from which we want to capture an argument. The type of the
* argument and the process of encoding/decoding it in the path are represented
* by the MatchArg constructor argument.
*/
final case class PMatch[Hd, Tl <: Pattern](head: MatchArg[Hd], tail: Tl) 
           extends PCons[Hd, Tl](head, tail) {
  type This = PMatch[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, Tl#Result]

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
    head.encode(args.head) ::
    tail.encode(args.tail.asInstanceOf[PMatch.this.tail.Result])
}

class PNil extends Pattern {
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

/**
* Useful functions for converting HLists to and from tuples of various lengths.
*/
object PatternOps {
  implicit def string2Arg(v: String):ConstArg = ConstArg(v)

  // implicit def hnilToUnit(v: HNil): Unit =
  //   ()
  // 
  // implicit def unitToHnil(t: Unit): HNil =
  //   HNil
  
  implicit def hlistToTuple1[A](v: HCons[A, HNil]): Tuple1[A] =
    Tuple1(v.head)
  
  implicit def tuple1ToHList[A](t: Tuple1[A]): HCons[A, HNil] =
    HCons(t._1, HNil)

  implicit def hlistToTuple2[A, B](v: HCons[A, HCons[B, HNil]]): Tuple2[A, B] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
 
    Tuple2(h1, h2)
  }
  
  implicit def tuple2ToHList[A, B](t: Tuple2[A, B]): HCons[A, HCons[B, HNil]] =
    HCons(t._1, HCons(t._2, HNil))
  
  implicit def hlistToTuple3[A, B, C](v: HCons[A, HCons[B, HCons[C, HNil]]]): Tuple3[A, B, C] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
 
    Tuple3(h1, h2, h3)
  }
  
  implicit def tuple3ToHList[A, B, C](t: Tuple3[A, B, C]): HCons[A, HCons[B, HCons[C, HNil]]] =
    HCons(t._1, HCons(t._2, HCons(t._3, HNil)))

  implicit def hlistToTuple4[A, B, C, D](v: HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]]): Tuple4[A, B, C, D] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
 
    Tuple4(h1, h2, h3, h4)
  }
  
  implicit def tuple4ToHList[A, B, C, D](t: Tuple4[A, B, C, D]): HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] =
    HCons(t._1, HCons(t._2, HCons(t._3, HCons(t._4, HNil))))

  implicit def hlistToTuple5[A, B, C, D, E](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]]): Tuple5[A, B, C, D, E] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
    val t4 = t3.tail
    val h5 = t4.head
 
    Tuple5(h1, h2, h3, h4, h5)
  }

  implicit def tuple5ToHList[A, B, C, D, E](t: Tuple5[A, B, C, D, E]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] =
    HCons(t._1, HCons(t._2, HCons(t._3, HCons(t._4, HCons(t._5, HNil)))))

  implicit def hlistToTuple6[A, B, C, D, E, F](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]]): Tuple6[A, B, C, D, E, F] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
    val t4 = t3.tail
    val h5 = t4.head
    val t5 = t4.tail
    val h6 = t5.head
 
    Tuple6(h1, h2, h3, h4, h5, h6)
  }

  implicit def tuple6ToHList[A, B, C, D, E, F](t: Tuple6[A, B, C, D, E, F]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]] =
    HCons(t._1, HCons(t._2, HCons(t._3, HCons(t._4, HCons(t._5, HCons(t._6, HNil))))))

  implicit def hlistToTuple7[A, B, C, D, E, F, G](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]]): Tuple7[A, B, C, D, E, F, G] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
    val t4 = t3.tail
    val h5 = t4.head
    val t5 = t4.tail
    val h6 = t5.head
    val t6 = t5.tail
    val h7 = t6.head
 
    Tuple7(h1, h2, h3, h4, h5, h6, h7)
  }

  implicit def tuple7ToHList[A, B, C, D, E, F, G](t: Tuple7[A, B, C, D, E, F, G]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]] =
    HCons(t._1, HCons(t._2, HCons(t._3, HCons(t._4, HCons(t._5, HCons(t._6, HCons(t._7, HNil)))))))
}

// ----------------------------------------------
// HList Implementation based on Mark Harrah's Up
// https://github.com/harrah/up

sealed trait HList {
	type Head
	type Tail <: HList
}

final case class HCons[H, T <: HList](val head : H, val tail : T) extends HList {
	type Head = H
	type Tail = T
}

sealed class HNil extends HList {
	type Head = Nothing
	type Tail = HNil
}

case object HNil extends HNil

