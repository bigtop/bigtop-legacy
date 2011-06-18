package bigtop
package routes

//import up.{HList, HNil, HCons, Fold}
//import up.HList._

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
  def /:[V](v: Arg[V]): PCons[V, This]
}

// A PCons cells where the term evaluates to Unit on a
// successful match, and hence its result is not included in
// the result.
final case class PLiteral[Hd, Tl <: Pattern](head: Arg[Hd], tail: Tl) 
           extends PCons[Hd, Tl](head, tail) {
  type This = PLiteral[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = Tl#Result
  
  
  def /:[V](v: Arg[V]): PCons[V, This] = v match {
    case v:Arg[Unit] => PLiteral[V, This](v, this)
    case _ => PMatch[V, This](v, this)
  }

  def decode(path: List[String]): Option[Result] =
    path match {
      case x :: xs => 
        if (head.decode(x).isDefined) tail.decode(xs) else None
      case Nil => None
    }     
}

// A PCons cell where the term evaluates to something interesting
final case class PMatch[Hd, Tl <: Pattern](head: Arg[Hd], tail: Tl) 
           extends PCons[Hd, Tl](head, tail) {
  type This = PMatch[Hd, Tl]
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, Tl#Result]

  def /:[V](v: Arg[V]): PCons[V, This] = v match {
    case v:Arg[Unit] => PLiteral[V, This](v, this)
    case _ => PMatch[V, This](v, this)
  }

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

  def /:[V](v: Arg[V]): PCons[V, This] = v match {
    case v:Arg[Unit] => PLiteral[Unit, This](v, this)
    case _ => PMatch[V, This](v, this)
  }

  def decode(path: List[String]): Option[Result] =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }
}
case object PNil extends PNil

object PatternOps {
  implicit def string2Arg(v: String):ConstArg = ConstArg(v)
}

//----------------------------------------------------------
// HList Implementation based on Mark Harrah's Up
// https://github.com/harrah/up
//----------------------------------------------------------

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

