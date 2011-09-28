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

import org.scalatest._

import HListOps._
import ArgOps._

class PathSuite extends FunSuite with Assertions {
  
  // Type-safe versions of expect()() -----------
  
  def expectTuple[A, B](v1: Tuple1[A], v2: Tuple1[B]) =
    expect(v1)(v2)
  
  def expectTuple[A,B](v1: Tuple2[A,B], v2: Tuple2[A,B]) =
    expect(v1)(v2)
  
  def expectTuple[A,B,C](v1: Tuple3[A,B,C], v2: Tuple3[A,B,C]) =
    expect(v1)(v2)

  def expectTuple[A,B,C,D](v1: Tuple4[A,B,C,D], v2: Tuple4[A,B,C,D]) =
    expect(v1)(v2)
  
  val PNil = new PNil(null)
  
  // Tests --------------------------------------
  
  test("/") {
    val p = PNil

    expect(Some(HNil))(p decode Nil)
    expect(None)(p decode List(""))
  }

  test("/ const") {
    val p = PNil / "abc"
    expect(Some(HNil))(p decode List("abc"))
    expect(None      )(p decode List("ab"))
    expect(None      )(p decode List("abcd"))
    expect(None      )(p decode Nil)
    
    expect(List("abc"))(p encode HNil)
  }

  test("/ arg") {
    val p = PNil / IntArg
    expectTuple(Tuple1(123), (p decode List("123")).get)

    expect(None)(p decode List("abc"))
    expect(None)(p decode Nil)
    
    expect(List("123"))(p encode Tuple1(123))
  }

  test("/ const / const") {
    val p = PNil / "abc" / "def"
    expect(Some(HNil))(p decode List("abc", "def"))
    expect(None      )(p decode List("def", "abc"))
    
    expect(List("abc", "def"))(p encode HNil)
  }

  test("/ const / arg") {
    val p = PNil / "abc" / IntArg
    expectTuple(Tuple1(123), (p decode List("abc", "123")).get)

    expect(None)(p decode List("abc", "def"))
    
    expect(List("abc", "123"))(p encode Tuple1(123))
  }

  test("/ arg / const") {
    val p = PNil / IntArg / "def"
    expectTuple(Tuple1(123), (p decode List("123", "def")).get)

    expect(None)(p decode List("abc", "def"))
    
    expect(List("123", "def"))(p encode Tuple1(123))
  }

  test("/ arg / arg") {
    val p = PNil / IntArg / IntArg
    expectTuple((123, 456), (p decode List("123", "456")).get)
    expectTuple((456, 123), (p decode List("456", "123")).get)

    expect(None)(p decode List("123", "abc"))
    
    expect(List("123", "456"))(p encode (123, 456))
    expect(List("456", "123"))(p encode (456, 123))
  }

  test("/ arg / arg / const") {
    val p = PNil / IntArg / StringArg / "abc"
    expectTuple((123, "456"), (p decode List("123", "456", "abc")).get)
    expectTuple((456, "123"), (p decode List("456", "123", "abc")).get)

    expect(None)(p decode List("abc", "456", "abc"))
    
    expect(List("123", "456", "abc"))(p encode (123, "456"))
    expect(List("456", "123", "abc"))(p encode (456, "123"))
  }

  test("/ arg / arg / arg") {
    val p = PNil / IntArg / StringArg / IntArg
    expectTuple((123, "456", 789), (p decode List("123", "456", "789")).get)
    expectTuple((789, "456", 123), (p decode List("789", "456", "123")).get)

    expect(None)(p decode List("123", "456"))

    expect(List("123", "456", "789"))(p encode (123, "456", 789))
  }
    
  test("/ arg / arg / arg / const") {
    val p = PNil / IntArg / StringArg / IntArg / "ghi"
    expectTuple((123, "456", 789), (p decode List("123", "456", "789", "ghi")).get)

    expect(None)(p decode List("abc", "456", "789", "ghi"))

    expect(List("123", "456", "789", "ghi"))(p encode (123, "456", 789))
  }

  test("/ arg / arg / arg / arg") {
    val p = PNil / IntArg / StringArg / IntArg / StringArg
    expectTuple((123, "456", 789, "012"), (p decode List("123", "456", "789", "012")).get)
    expectTuple(( 12, "789", 456, "123"), (p decode List("012", "789", "456", "123")).get)

    expect(List("123", "456", "789", "012"))(p encode (123, "456", 789, "012"))
  }

}
