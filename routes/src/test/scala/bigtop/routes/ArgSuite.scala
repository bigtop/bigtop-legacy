package bigtop
package routes

import org.scalatest._

class ArgSuite extends FunSuite with Assertions {
  
  test("IntArg") {
    expect("123")(IntArg encode 123)
    expect("-123")(IntArg encode -123)
    expect(Some(123))(IntArg decode "123")
    expect(Some(-123))(IntArg decode "-123")
    expect(None)(IntArg decode "abc")
  }
  
  test("StringArg") {
    expect("a%2Fb")(StringArg encode "a/b")
    expect(Some("a/b"))(StringArg decode "a%2Fb")
  }
  
}