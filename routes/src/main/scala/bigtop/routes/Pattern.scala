package bigtop
package routes

case class PatternException(msg: String, value: Any) extends Exception(msg) {
  override def toString: String =
    msg + " " + value.toString
}

sealed trait HList
 
final case class HCons[H, T <: HList](head : H, tail : T) extends HList {
  def ::[N](n : N) = HCons(n, this)
}
 
sealed class HNil extends HList {
  def ::[N](n : N) = HCons(n, this)
}

final object HNil extends HNil
 
// aliases for building HList types and for pattern matching
object HList {
  type ::[H, T <: HList] = HCons[H, T]
  val :: = HCons
}

trait ListPattern[L <: HList] {

  def encodeHList(value: L): List[String]
  def decodeHList(path: List[String]): Option[L]

  def encodeError(in: Any) = throw new PatternException("Could not encode", in)
  def decodeError(in: Any) = throw new PatternException("Could not decode", in)
  
}

case object NilPattern extends ListPattern[HNil] { // with Pattern0 {
  
  def encodeHList(value: HNil) = Nil
  
  def decodeHList(path: List[String]) =
    path match {
      case Nil => Some(HNil)
      case _ => None
    }

}

case class ConstPattern[L <: HList](val const: String, val base: ListPattern[L]) extends ListPattern[L] {
  
  def encodeHList(value: L) =
    const :: base.encodeHList(value)
  
  def decodeHList(path: List[String]) =
    path match {
      case h :: t if h == const => base.decodeHList(t)
      case _ => None
    }
  
}

case class ArgPattern[T, L <: HList](val arg: Arg[T], val base: ListPattern[L]) extends ListPattern[HCons[T, L]] {
  
  def encodeHList(value: HCons[T, L]) =
    arg.encode(value.head) :: base.encodeHList(value.tail)
  
  def decodeHList(path: List[String]) =
    path match {
      case h :: t =>
        for {
          h <- arg.decode(h)
          t <- base.decodeHList(t)
        } yield { h :: t }
      case _ => None
    }

}

// trait Pattern[T] extends ListPattern {
// 
//   def encode(value: T): List[String]
//     
//   def decode(path: List[String]): Option[T]
//   
// }
// 
// trait Pattern0 extends Pattern[Unit] {
//   
//   def encode(value: Unit): List[String] =
//     apply(Nil)
// 
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(Nil) => Some(())
//       case _ => None
//     }
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern0
//   
//   def /[A](head: Arg[A]) =
//    new ArgPattern(head, this) with Pattern1[A]
//   
// }
// 
// trait PatternN[T <: Product] extends Pattern[T] {
//  
//   def encode(value: T): List[String] =
//     apply(value.productIterator.toList)
//  
// }
// 
// trait Pattern1[A] extends PatternN[Tuple1[A]] {
//   
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(a :: Nil) => Some(Tuple1(a.asInstanceOf[A]))
//       case _ => None
//     }
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern1[A]
//   
//   def /[B](head: Arg[B]) =
//      new ArgPattern(head, this) with Pattern2[A, B]
//   
// }
// 
// trait Pattern2[A, B] extends PatternN[(A, B)] {
//   
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(b :: a :: Nil) =>
//         Some((a.asInstanceOf[A], 
//               b.asInstanceOf[B]))
//       
//       case _ => None
//     } 
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern2[A, B]
//   
//   def /[C](head: Arg[C]) =
//     new ArgPattern(head, this) with Pattern3[A, B, C]
//   
// }
// 
// trait Pattern3[A, B, C] extends PatternN[(A, B, C)] {
//   
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(c :: b :: a :: Nil) =>
//         Some((a.asInstanceOf[A], 
//               b.asInstanceOf[B],
//               c.asInstanceOf[C]))
//       
//       case _ => None
//     } 
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern3[A, B, C]
//   
//   def /[D](head: Arg[D]) =
//     new ArgPattern(head, this) with Pattern4[A, B, C, D]
//   
// }
// 
// trait Pattern4[A, B, C, D] extends PatternN[(A, B, C, D)] {
//   
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(d :: c :: b :: a :: Nil) =>
//         Some((a.asInstanceOf[A], 
//               b.asInstanceOf[B],
//               c.asInstanceOf[C],
//               d.asInstanceOf[D]))
//       
//       case _ => None
//     } 
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern4[A, B, C, D]
//   
//   // def /[E](head: Arg[E]) =
//   //   new ArgPattern(head, this) with Pattern5[A, B, C, D, E]
//   
// }
// 
// trait Pattern5[A, B, C, D, E] extends PatternN[(A, B, C, D, E)] {
//   
//   def decode(path: List[String]) =
//     unapply(path.reverse) match {
//       case Some(e :: d :: c :: b :: a :: Nil) =>
//         Some((a.asInstanceOf[A], 
//               b.asInstanceOf[B],
//               c.asInstanceOf[C],
//               d.asInstanceOf[D],
//               e.asInstanceOf[E]))
//       
//       case _ => None
//     } 
//   
//   def /(head: String) =
//     new ConstPattern(head, this) with Pattern5[A, B, C, D, E]
//   
//   // def /[F](head: Arg[F]) =
//   //   new ArgPattern(head, this) with Pattern6[A, B, C, D, E, F]
//   
// }
