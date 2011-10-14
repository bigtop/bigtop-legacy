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

object HListOps {
  
  implicit def hlistFunction0[A, Res](fn: () => Res) =
    (in: HNil) => {
      fn()
    }
  
  implicit def hlistFunction1[A, Res](fn: (A) => Res) =
    (in: HCons[A, HNil]) => {
      val h1 = in.head
      
      fn(h1)
    }
  
  implicit def hlistFunction2[A, B, Res](fn: (A, B) => Res) =
    (in: HCons[A, HCons[B, HNil]]) => {
      val h1 = in.head
      val t1 = in.tail
      val h2 = t1.head
      
      fn(h1, h2)
    }
  
  implicit def hlistFunction3[A, B, C, Res](fn: (A, B, C) => Res) =
    (in: HCons[A, HCons[B, HCons[C, HNil]]]) => {
      val h1 = in.head
      val t1 = in.tail
      val h2 = t1.head
      val t2 = t1.tail
      val h3 = t2.head
      
      fn(h1, h2, h3)
    }
  
  implicit def hlistFunction4[A, B, C, D, Res](fn: (A, B, C, D) => Res) =
    (in: HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]]) => {
      val h1 = in.head
      val t1 = in.tail
      val h2 = t1.head
      val t2 = t1.tail
      val h3 = t2.head
      val t3 = t2.tail
      val h4 = t3.head
      
      fn(h1, h2, h3, h4)
    }
  
  implicit def hlistFunction5[A, B, C, D, E, Res](fn: (A, B, C, D, E) => Res) =
    (in: HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]]) => {
      val h1 = in.head
      val t1 = in.tail
      val h2 = t1.head
      val t2 = t1.tail
      val h3 = t2.head
      val t3 = t2.tail
      val h4 = t3.head
      val t4 = t3.tail
      val h5 = t4.head
      
      fn(h1, h2, h3, h4, h5)
    }
  
}
