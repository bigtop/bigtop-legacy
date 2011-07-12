package bigtop
package routes

import org.scalatest._

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.LiftResponse

class SiteSuite extends FunSuite with Assertions {
  
  val emptyOp: () => Box[LiftResponse] = () => Empty
  val intOp: Int => Box[LiftResponse] = i => Empty

  object MySite extends Site {
    
    val listContacts = add("contacts" /: PNil >> emptyOp)

    val newContact = add(("contacts" /: "new" /: PNil) >> emptyOp)

    val viewContact = add(("contacts" /: IntArg /: PNil) >> intOp)

    val editContact = add(("contacts" /: IntArg /: "edit" /: PNil) >> intOp)

    val deleteContact = add(("contacts" /: IntArg /: "delete" /: PNil) >> intOp)

  }

  test("viewContact url") {
    expect("/contacts/123")(MySite.newContact.url(123))
  }

}
