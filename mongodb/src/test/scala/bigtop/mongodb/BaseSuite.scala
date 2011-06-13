package bigtop
package mongodb

import org.scalatest._

class BaseSuite extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Assertions {

  override def beforeAll: Unit =
    TestDb.init

  override def beforeEach(): Unit =
    TestDb.dropAndRecreateDatabase

}
