package bigtop
package routes

import org.scalatest._

class PatternSuite extends FunSuite with Assertions {

  test("/") {
    val p = NilPattern

    expect(Some(()))(p decode Nil)
    expect(None)(p decode List(""))
  }

  test("/ const") {
    val p = NilPattern / "abc"
    expect(Some(()))(p decode List("abc"))
    expect(None    )(p decode List("ab"))
    expect(None    )(p decode List("abcd"))
    expect(None    )(p decode Nil)
  }

  test("/ arg") {
    val p = NilPattern / IntArg
    expect(Some(Tuple1(123)))(p decode List("123"))
    expect(None       )(p decode List("abc"))
    expect(None       )(p decode Nil)
  }

  test("/ const / const") {
    val p = NilPattern / "abc" / "def"
    expect(Some(()))(p decode List("abc", "def"))
    expect(None    )(p decode List("def", "abc"))
  }

  test("/ const / arg") {
    val p = NilPattern / "abc" / IntArg
    expect(Some(Tuple1(123)))(p decode List("abc", "123"))
    expect(None             )(p decode List("abc", "def"))
  }

  test("/ arg / const") {
    val p = NilPattern / IntArg / "def"
    expect(Some(Tuple1(123)))(p decode List("123", "def"))
    expect(None             )(p decode List("abc", "def"))
  }

  test("/ arg / arg") {
    val p = NilPattern / IntArg / IntArg
    expect(Some((123, 456)))(p decode List("123", "456"))
    expect(Some((456, 123)))(p decode List("456", "123"))
    expect(None            )(p decode List("123", "abc"))
  }

  test("/ arg / arg / const") {
    val p = NilPattern / IntArg / StringArg / "abc"
    expect(Some((123, "456")))(p decode List("123", "456", "abc"))
    expect(Some((456, "123")))(p decode List("456", "123", "abc"))
    expect(None              )(p decode List("abc", "456", "abc"))
  }

  test("/ arg / arg / arg") {
    val p = NilPattern / IntArg / StringArg / IntArg
    expect(Some((123, "456", 789)))(p decode List("123", "456", "789"))
    expect(Some((789, "456", 123)))(p decode List("789", "456", "123"))
    expect(None                   )(p decode List("123", "456"))
  }
    
  test("/ arg / arg / arg / const") {
    val p = NilPattern / IntArg / StringArg / IntArg / "ghi"
    expect(Some((123, "456", 789)))(p decode List("123", "456", "789", "ghi"))
    expect(None                   )(p decode List("abc", "456", "789", "ghi"))
  }

  test("/ arg / arg / arg / arg") {
    val p = NilPattern / IntArg / StringArg / IntArg / StringArg
    expect(Some((123, "456", 789, "012")))(p decode List("123", "456", "789", "012"))
    expect(Some(( 12, "789", 456, "123")))(p decode List("012", "789", "456", "123"))
  }
  
}