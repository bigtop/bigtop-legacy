package bigtop
package routes

import org.scalatest._

import PatternOps._

class PatternSuite extends FunSuite with Assertions {

  def expectTuple[A, B](v1: Tuple1[A], v2: Tuple1[B]) =
    expect(v1)(v2)
  def expectTuple[A,B](v1: Tuple2[A,B], v2: Tuple2[A,B]) =
    expect(v1)(v2)
  def expectTuple[A,B,C](v1: Tuple3[A,B,C], v2: Tuple3[A,B,C]) =
    expect(v1)(v2)
  def expectTuple[A,B,C,D](v1: Tuple4[A,B,C,D], v2: Tuple4[A,B,C,D]) =
    expect(v1)(v2)


  test("/") {
    val p = PNil

    expect(Some(HNil))(p decode Nil)
    expect(None)(p decode List(""))
  }

  test("/ const") {
    val p = "abc" /: PNil
    expect(Some(HNil))(p decode List("abc"))
    expect(None      )(p decode List("ab"))
    expect(None      )(p decode List("abcd"))
    expect(None      )(p decode Nil)
  }

  test("/ arg") {
    val p = IntArg /: PNil
    expectTuple(Tuple1(123), (p decode List("123")).get)

    expect(None)(p decode List("abc"))
    expect(None)(p decode Nil)
  }

  test("/ const / const") {
    val p = "abc" /: "def" /: PNil
    expect(Some(HNil))(p decode List("abc", "def"))
    expect(None      )(p decode List("def", "abc"))
  }

  test("/ const / arg") {
    val p = "abc" /: IntArg /: PNil
    expectTuple(Tuple1(123), (p decode List("abc", "123")).get)

    expect(None)(p decode List("abc", "def"))
  }

  test("/ arg / const") {
    val p = "def" /: IntArg /: PNil
    expectTuple(Tuple1(123), (p decode List("def", "123")).get)

    expect(None)(p decode List("abc", "def"))
  }

  test("/ arg / arg") {
    val p = IntArg /: IntArg /: PNil
    expectTuple((123, 456), (p decode List("123", "456")).get)
    expectTuple((456, 123), (p decode List("456", "123")).get)

    expect(None)(p decode List("123", "abc"))
  }

  test("/ arg / arg / const") {
    val p = IntArg /: StringArg /: "abc" /: PNil
    expectTuple((123, "456"), (p decode List("123", "456", "abc")).get)
    expectTuple((456, "123"), (p decode List("456", "123", "abc")).get)

    expect(None)(p decode List("abc", "456", "abc"))
  }

  test("/ arg / arg / arg") {
    val p = IntArg /: StringArg /: IntArg /: PNil
    expectTuple((123, "456", 789), (p decode List("123", "456", "789")).get)
    expectTuple((789, "456", 123), (p decode List("789", "456", "123")).get)

    expect(None)(p decode List("123", "456"))
  }
    
  test("/ arg / arg / arg / const") {
    val p = IntArg /: StringArg /: IntArg /: "ghi" /: PNil
    expectTuple((123, "456", 789), (p decode List("123", "456", "789", "ghi")).get)
    expect(None)(p decode List("abc", "456", "789", "ghi"))
  }

  test("/ arg / arg / arg / arg") {
    val p = IntArg /: StringArg /: IntArg /: StringArg /: PNil
    expectTuple((123, "456", 789, "012"), (p decode List("123", "456", "789", "012")).get)
    expectTuple(( 12, "789", 456, "123"), (p decode List("012", "789", "456", "123")).get)
  }
  
}
