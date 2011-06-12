package bigtop
package routes

import org.scalatest._

class ArgSuite extends FunSuite with Assertions {
  
  test("IntArg") {
    expect("123")(IntArg apply 123)
    expect("-123")(IntArg apply -123)
    expect(Some(123))(IntArg unapply "123")
    expect(Some(-123))(IntArg unapply "-123")
    expect(None)(IntArg unapply "abc")
  }
  
  test("StringArg") {
    expect("a%2Fb")(StringArg apply "a/b")
    expect(Some("a/b"))(StringArg unapply "a%2Fb")
  }
  
}