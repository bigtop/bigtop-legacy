package bigtop
package routes

import up.{HList, HNil, HCons, Fold}
import up.HList._

case class PatternException(msg: String, value: Any) extends Exception(msg) {
  override def toString: String =
    msg + " " + value.toString
}

class UrlPattern[P <: PList](pList: P) {  
  def decode(path: List[String]): Option[P#Result] = pList.decode(path)
}


// A Pattern list
sealed trait PList {
  type Head
  type Tail <: PList
  type Result 

  def decode(path: List[String]): Option[Result]
}

final case class PConsNull[Tl <: PList, TR <: HList](head: Arg[Unit], tail: Tl) 
  extends PList {
   type Head = Arg[Unit]
   type Tail = Tl
   type Result = TR

   def /:[V](v: Arg[V]) = v match {
     case v:Arg[Unit] => PConsNull[PConsNull[Tl, Result], Result](v, this)
     case _ => PCons[V, PConsNull[Tl, Result], Result](v, this)
   }

  def decode(path: List[String]): Option[Result] =
    path match {
      case Pair(x:String, xs:List[String]) => 
        if (head.decode(x).isDefined) tail.decode(xs) else None
      case Nil => None
    }     
}

final case class PCons[Hd, Tl <: PList, TR <: HList](head: Arg[Hd], tail: Tl)
 extends PList {
  type Head = Arg[Hd]
  type Tail = Tl
  type Result = HCons[Hd, TR]

  def /:[V](v: Arg[V]) = v match {
    case v:Arg[Unit] => PConsNull[PCons[Hd, Tl, Result], Result](v, this)
    case _ => PCons[V, PCons[Hd, Tl, Result], Result](v, this)
  }

  def decode(path: List[String]): Option[Result] =
    path match {
      case Pair(x:String, xs:List[String]) => 
        head.decode(x).flatMap{ v => 
          tail.decode(xs) match {
            case None     => None
            case Some(vs) => Some(HCons(v, vs))
          }
        }
      case Nil => None
    }

}

case class PNil() extends PList {
  type Head = Nothing
  type Tail = PNil
  type Result = HNil

  def /:[V](v: Arg[V]) = v match {
    case v:Arg[Unit] => PCons[Unit, PNil, HNil](v, this)
    case _ => PCons[V, PNil, HCons[V, HNil]](v, this)
  }

  def decode(path: List[String]): Option[Result] =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }
}
object PNil extends PNil




// trait ListPattern[L <: HList] {

//   def encodeHList(value: L): List[String]
//   def decodeHList(path: List[String]): Option[L]

//   def encodeError(in: Any) = throw new PatternException("Could not encode", in)
//   def decodeError(in: Any) = throw new PatternException("Could not decode", in)
  
// }

// case object NilPattern extends ListPattern[HNil] { // with Pattern0 {
  
//   def encodeHList(value: HNil) = Nil
  
//   def decodeHList(path: List[String]) =
//     path match {
//       case Nil => Some(HNil)
//       case _ => None
//     }

// }

// case class ConstPattern[L <: HList](val const: String, val base: ListPattern[L]) extends ListPattern[L] {
  
//   def encodeHList(value: L) =
//     const :: base.encodeHList(value)
  
//   def decodeHList(path: List[String]) =
//     path match {
//       case h :: t if h == const => base.decodeHList(t)
//       case _ => None
//     }
  
// }

// case class ArgPattern[T, L <: HList](val arg: Arg[T], val base: ListPattern[L]) extends ListPattern[HCons[T, L]] {
  
//   def encodeHList(value: HCons[T, L]) =
//     arg.encode(value.head) :: base.encodeHList(value.tail)
  
//   def decodeHList(path: List[String]) =
//     path match {
//       case h :: t =>
//         for {
//           h <- arg.decode(h)
//           t <- base.decodeHList(t)
//         } yield { h :: t }
//       case _ => None
//     }

// }

