package bigtop
package routes

case class PatternException(msg: String, value: Any) extends Exception(msg) {
  override def toString: String =
    msg + " " + value.toString
}

trait ListPattern {

  def apply(value: List[_]): List[String]
  def unapply(path: List[String]): Option[List[_]]
  
  def encodeError(in: Any) =
    throw new PatternException("Could not encode", in)
  
}

case object NilPattern extends ListPattern with Pattern0 {
  
  def apply(value: List[_]) =
    Nil
  
  def unapply(path: List[String]) =
    path match {
      case Nil =>
        Some(Nil)
      
      case other =>
        None
    }

}

case class ConstPattern(val Head: String, val Tail: ListPattern) extends ListPattern {
  
  def apply(value: List[_]) =
    Head :: Tail(value)
  
  def unapply(path: List[String]) =
    path match {
      case Head :: Tail(tail) =>
        Some(tail)
      
      case other =>
        None
    }
  
}

case class ArgPattern[T](val Head: Arg[T], val Tail: ListPattern) extends ListPattern {
  
  def apply(value: List[Any]): List[String] =
    value match {
      case head :: tail =>
        Head(head.asInstanceOf[T]) :: Tail(tail)
        
      case other =>
        encodeError(other)
    }
  
  def unapply(path: List[String]) =
    path match {
      case Head(head) :: Tail(tail) =>
        Some(head :: tail)
      
      case other =>
        None
    }

}

trait Pattern[T] extends ListPattern {

  def encode(value: T): List[String]
    
  def decode(path: List[String]): Option[T]
  
}

trait Pattern0 extends Pattern[Unit] {
  
  def encode(value: Unit): List[String] =
    apply(Nil)

  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(Nil) => Some(())
      case _ => None
    }
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern0
  
  def /[A](head: Arg[A]) =
   new ArgPattern(head, this) with Pattern1[A]
  
}

trait PatternN[T <: Product] extends Pattern[T] {
 
  def encode(value: T): List[String] =
    apply(value.productIterator.toList)
 
}

trait Pattern1[A] extends PatternN[Tuple1[A]] {
  
  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(a :: Nil) => Some(Tuple1(a.asInstanceOf[A]))
      case _ => None
    }
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern1[A]
  
  def /[B](head: Arg[B]) =
     new ArgPattern(head, this) with Pattern2[A, B]
  
}

trait Pattern2[A, B] extends PatternN[(A, B)] {
  
  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(b :: a :: Nil) =>
        Some((a.asInstanceOf[A], 
              b.asInstanceOf[B]))
      
      case _ => None
    } 
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern2[A, B]
  
  def /[C](head: Arg[C]) =
    new ArgPattern(head, this) with Pattern3[A, B, C]
  
}

trait Pattern3[A, B, C] extends PatternN[(A, B, C)] {
  
  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(c :: b :: a :: Nil) =>
        Some((a.asInstanceOf[A], 
              b.asInstanceOf[B],
              c.asInstanceOf[C]))
      
      case _ => None
    } 
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern3[A, B, C]
  
  def /[D](head: Arg[D]) =
    new ArgPattern(head, this) with Pattern4[A, B, C, D]
  
}

trait Pattern4[A, B, C, D] extends PatternN[(A, B, C, D)] {
  
  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(d :: c :: b :: a :: Nil) =>
        Some((a.asInstanceOf[A], 
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D]))
      
      case _ => None
    } 
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern4[A, B, C, D]
  
  // def /[E](head: Arg[E]) =
  //   new ArgPattern(head, this) with Pattern5[A, B, C, D, E]
  
}

trait Pattern5[A, B, C, D, E] extends PatternN[(A, B, C, D, E)] {
  
  def decode(path: List[String]) =
    unapply(path.reverse) match {
      case Some(e :: d :: c :: b :: a :: Nil) =>
        Some((a.asInstanceOf[A], 
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E]))
      
      case _ => None
    } 
  
  def /(head: String) =
    new ConstPattern(head, this) with Pattern5[A, B, C, D, E]
  
  // def /[F](head: Arg[F]) =
  //   new ArgPattern(head, this) with Pattern6[A, B, C, D, E, F]
  
}
