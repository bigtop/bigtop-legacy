package bigtop
package routes


case class PatternException(msg: String, value: Any) extends Exception(msg) {
  override def toString: String =
    msg + " " + value.toString
}

class UrlPattern[P <: Pattern](pList: P) {  
  def decode(path: List[String]): Option[P#Result] = pList.decode(path)
}

// A Pattern is a list of terms (currently called Args)
//
// The list is constructed of PNil and PCons structures --
// needed to propagate the types of the terms.

// A Pattern list
sealed trait Pattern {
  type This <: Pattern
  type Head
  type Tail <: Pattern
  type Result <: HList // The result type if this pattern matches

  def decode(path: List[String]): Option[Result]
}

abstract sealed class PCons[Hd, Tl <: Pattern](head: Arg[Hd], tail: Tl) extends Pattern {
  def /:(v: LiteralArg): PLiteral[This]
  def /:[V](v: MatchArg[V]): PMatch[V, This]
}

// A PCons cells where the term evaluates to Unit on a
// successful match, and hence its result is not included in
// the result.
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
}

// A PCons cell where the term evaluates to something interesting
final case class PMatch[Hd, Tl <: Pattern](head: MatchArg[Hd], tail: Tl) 
           extends PCons[Hd, Tl](head, tail) {
  type This = PMatch[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, Tl#Result]

  def /:(v: LiteralArg): PLiteral[This] = PLiteral[This](v, this)
  def /:[V](v: MatchArg[V]): PMatch[V, This] = PMatch[V, This](v, this)

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
}

class PNil extends Pattern {
  type This = PNil
  type Head = Nothing
  type Tail = PNil
  type Result = HNil

  def /:(v: LiteralArg): PLiteral[This] = PLiteral[This](v, this)
  def /:[V](v: MatchArg[V]): PMatch[V, This] = PMatch[V, This](v, this)

  def decode(path: List[String]): Option[Result] =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }
}
case object PNil extends PNil

object PatternOps {
  implicit def string2Arg(v: String):ConstArg = ConstArg(v)

  implicit def hlist2Tuple1[V](v: HCons[V, HNil]): Tuple1[V] = Tuple1(v.head)

  implicit def hlist2Tuple2[A, B](v: HCons[A, HCons[B, HNil]]): Tuple2[A, B] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
 
    Tuple2(h1, h2)
  }

  implicit def hlist2Tuple3[A, B, C](v: HCons[A, HCons[B, HCons[C, HNil]]]): Tuple3[A, B, C] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
 
    Tuple3(h1, h2, h3)
  }

  implicit def hlist2Tuple4[A, B, C, D](v: HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]]): Tuple4[A, B, C, D] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
 
    Tuple4(h1, h2, h3, h4)
  }

  implicit def hlist2Tuple5[A, B, C, D, E](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]]): Tuple5[A, B, C, D, E] = {
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

  implicit def hlist2Tuple6[A, B, C, D, E, F](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]]): Tuple6[A, B, C, D, E, F] = {
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

  implicit def hlist2Tuple7[A, B, C, D, E, F, G](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]]): Tuple7[A, B, C, D, E, F, G] = {
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
}

//----------------------------------------------------------
// HList Implementation based on Mark Harrah's Up
// https://github.com/harrah/up

sealed trait HList
{
	type Head
	type Tail <: HList
}

final case class HCons[H, T <: HList](head : H, tail : T) extends HList
{
	type Head = H
	type Tail = T
}
sealed class HNil extends HList
{
	type Head = Nothing
	type Tail = HNil
}
case object HNil extends HNil

