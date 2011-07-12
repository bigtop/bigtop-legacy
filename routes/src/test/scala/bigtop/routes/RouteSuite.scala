package bigtop
package routes

import org.scalatest._
import net.liftweb.common.{Box,Full,Empty}

class RouteSuite extends FunSuite with Assertions {

  import HListOps._
  import ArgOps._

  test("Route construction with varying implicits type checks") {
    "foo" /: "bar" /: PNil >> (() => Empty)
    "foo" /: ConstArg("bar") /: PNil >> (() => Empty)
    "foo" /: "bar" /: PNil >> (() => Empty)
  }

}
