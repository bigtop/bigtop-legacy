package bigtop
package squeryl

import org.scalatest._

class BaseSuite extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Assertions {
  override def beforeAll: Unit =
    TestDb.init

  override def withFixture(test: NoArgTest): Unit =
    TestDb.dropAndRecreateDatabase 
}
