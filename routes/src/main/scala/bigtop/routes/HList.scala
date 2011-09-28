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

trait HListFunction[A <: HList, R] extends Function1[A, R] {
  def apply(in: A): R
}

trait HListOps {
  
  // Tuples to HLists ---------------------------
  
  implicit def hlistToTuple1[A](v: HCons[A, HNil]): Tuple1[A] =
    Tuple1(v.head)
  
  implicit def tuple1ToHList[A](t: Tuple1[A]): HCons[A, HNil] =
    HCons(t._1, HNil)

  implicit def hlistToTuple2[A, B](v: HCons[A, HCons[B, HNil]]): Tuple2[B, A] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
 
    Tuple2(h2, h1)
  }
  
  implicit def tuple2ToHList[A, B](t: Tuple2[B, A]): HCons[A, HCons[B, HNil]] =
    HCons(t._2, HCons(t._1, HNil))
  
  implicit def hlistToTuple3[A, B, C](v: HCons[A, HCons[B, HCons[C, HNil]]]): Tuple3[C, B, A] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
 
    Tuple3(h3, h2, h1)
  }
  
  implicit def tuple3ToHList[A, B, C](t: Tuple3[C, B, A]): HCons[A, HCons[B, HCons[C, HNil]]] =
    HCons(t._3, HCons(t._2, HCons(t._1, HNil)))

  implicit def hlistToTuple4[A, B, C, D](v: HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]]): Tuple4[D, C, B, A] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
 
    Tuple4(h4, h3, h2, h1)
  }
  
  implicit def tuple4ToHList[A, B, C, D](t: Tuple4[D, C, B, A]): HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] =
    HCons(t._4, HCons(t._3, HCons(t._2, HCons(t._1, HNil))))

  implicit def hlistToTuple5[A, B, C, D, E](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]]): Tuple5[E, D, C, B, A] = {
    val h1 = v.head
    val t1 = v.tail
    val h2 = t1.head
    val t2 = t1.tail
    val h3 = t2.head
    val t3 = t2.tail
    val h4 = t3.head
    val t4 = t3.tail
    val h5 = t4.head
 
    Tuple5(h5, h4, h3, h2, h1)
  }

  implicit def tuple5ToHList[A, B, C, D, E](t: Tuple5[E, D, C, B, A]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] =
    HCons(t._5, HCons(t._4, HCons(t._3, HCons(t._2, HCons(t._1, HNil)))))

  implicit def hlistToTuple6[A, B, C, D, E, F](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]]): Tuple6[F, E, D, C, B, A] = {
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
 
    Tuple6(h6, h5, h4, h3, h2, h1)
  }

  implicit def tuple6ToHList[A, B, C, D, E, F](t: Tuple6[F, E, D, C, B, A]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]] =
    HCons(t._6, HCons(t._5, HCons(t._4, HCons(t._3, HCons(t._2, HCons(t._1, HNil))))))

  implicit def hlistToTuple7[A, B, C, D, E, F, G](v: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]]): Tuple7[G, F, E, D, C, B, A] = {
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
 
    Tuple7(h7, h6, h5, h4, h3, h2, h1)
  }

  implicit def tuple7ToHList[A, B, C, D, E, F, G](t: Tuple7[G, F, E, D, C, B, A]): HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]] =
    HCons(t._7, HCons(t._6, HCons(t._5, HCons(t._4, HCons(t._3, HCons(t._2, HCons(t._1, HNil)))))))

  implicit def function0ToHListFunction[Result](fn: () => Result) = {
    new HListFunction[HNil, Result] {
      def apply(in: HNil): Result = {
        fn()
      }
    }
  }
  
  // Functions to HList functions ---------------
  
  implicit def function1ToHListFunction[A, Result](fn: (A) => Result) =
    new HListFunction[HCons[A, HNil], Result] {
      def apply(in: HCons[A, HNil]): Result = {
        val h1 = in.head
        
        fn(h1)
      }
    }
  
  implicit def function2ToHListFunction[A, B, Result](fn: (B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HNil]], Result] {
      def apply(in: HCons[A, HCons[B, HNil]]): Result = {
        val h1 = in.head
        val t1 = in.tail
        val h2 = t1.head
        
        fn(h2, h1)
      }
    }
  
  implicit def function3ToHListFunction[A, B, C, Result](fn: (C, B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HCons[C, HNil]]], Result] {
      def apply(in: HCons[A, HCons[B, HCons[C, HNil]]]): Result = {
        val h1 = in.head
        val t1 = in.tail
        val h2 = t1.head
        val t2 = t1.tail
        val h3 = t2.head
        
        fn(h3, h2, h1)
      }
    }
  
  implicit def function4ToHListFunction[A, B, C, D, Result](fn: (D, C, B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]], Result] {
      def apply(in: HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]]): Result = {
        val h1 = in.head
        val t1 = in.tail
        val h2 = t1.head
        val t2 = t1.tail
        val h3 = t2.head
        val t3 = t2.tail
        val h4 = t3.head
        
        fn(h4, h3, h2, h1)
      }
    }
  
  implicit def function5ToHListFunction[A, B, C, D, E, Result](fn: (E, D, C, B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]], Result] {
      def apply(in: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]]): Result = {
        val h1 = in.head
        val t1 = in.tail
        val h2 = t1.head
        val t2 = t1.tail
        val h3 = t2.head
        val t3 = t2.tail
        val h4 = t3.head
        val t4 = t3.tail
        val h5 = t4.head
        
        fn(h5, h4, h3, h2, h1)
      }
    }
  
  implicit def function6ToHListFunction[A, B, C, D, E, F, Result](fn: (F, E, D, C, B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]], Result] {
      def apply(in: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HNil]]]]]]): Result = {
        val h1 = in.head
        val t1 = in.tail
        val h2 = t1.head
        val t2 = t1.tail
        val h3 = t2.head
        val t3 = t2.tail
        val h4 = t3.head
        val t4 = t3.tail
        val h5 = t4.head
        val t5 = t4.tail
        val h6 = t5.head
        
        fn(h6, h5, h4, h3, h2, h1)
      }
    }
  
  implicit def function7ToHListFunction[A, B, C, D, E, F, G, Result](fn: (G, F, E, D, C, B, A) => Result) =
    new HListFunction[HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]], Result] {
      def apply(in: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HCons[F, HCons[G, HNil]]]]]]]): Result = {
        val h1 = in.head
        val t1 = in.tail
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
        
        fn(h7, h6, h5, h4, h3, h2, h1)
      }
    }
  
}

object HListOps extends HListOps
