package bigtop
package record

import org.scalatest._

import org.scalacheck.Prop.forAll

class BCryptSuite extends FunSuite with Assertions {

  test("hashedLength is correct") {
    forAll { s: String => BCrypt.hash(s).length == BCrypt.hashedLength }
  }

  test("same password is hashed to different hash on successive hashes") {
    forAll { s: String => BCrypt.hash(s) != BCrypt.hash(s) }
  }

  test("compare is true for same plaintext") {
    forAll { s: String => BCrypt.compare(s, BCrypt.hash(s)) }
  }

  test("password is hashed to same string given same salt") {
    forAll { s: String => BCrypt.hash(s, "salty") == BCrypt.hash(s, "salty") }
  }

  test("isHash is true for BCrypt hashes") {
    forAll { s: String => BCrypt.isHash(BCrypt.hash(s)) }
  }

  test("isHash is false for normal strings") {
    // This test will very occassionally fail when a BCrypt
    // hash is randomly generated
    forAll { s: String => !BCrypt.isHash(s) }
  }
}
