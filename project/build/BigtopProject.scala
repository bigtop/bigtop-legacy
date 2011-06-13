import sbt._

class BigtopProject(info: ProjectInfo) extends ParentProject(info) {

  // Libraries ----------------------------------

  val liftVersion = "2.4-M2"
  val rogueVersion = "1.0.14-LIFT-2.4-M2"
  
  // Lift:
  lazy val rogue = "com.foursquare" %% "rogue" % rogueVersion withSources()
  lazy val liftMongodb = "net.liftweb" %% "lift-mongodb" % liftVersion % "compile"
  lazy val liftMongodbRecord = "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile"
  lazy val liftSquerylRecord = "net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile"
  lazy val liftTestkit = "net.liftweb" %% "lift-testkit" % liftVersion % "test"
  lazy val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  lazy val jettyTest = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"

  // Databases:
  lazy val postgresql = "postgresql" % "postgresql" % "8.4-702.jdbc4"

  // Test frameworks:
  lazy val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

  // Subprojects --------------------------------

  lazy val core = bigtopProject("core", liftWebkit, scalatest, liftTestkit, jettyTest)()
  lazy val debug = bigtopProject("debug", liftWebkit, scalatest)()
  lazy val report = bigtopProject("report", liftWebkit, scalatest)(debug, core)
  lazy val squeryl = bigtopProject("squeryl", liftWebkit, liftSquerylRecord, postgresql, scalatest)(debug)
  lazy val mongodb = bigtopProject("mongodb", liftWebkit, liftMongodb, liftMongodbRecord, rogue, scalatest)(debug)
  lazy val util = bigtopProject("util", liftWebkit, scalatest)(debug)
  
  // Helpers ------------------------------------

  val untypedResolver = {
    val host = System.getenv("DEFAULT_REPO_HOST")
    val path = System.getenv("DEFAULT_REPO_PATH")
    val user = System.getenv("DEFAULT_REPO_USER")
    val keyfile = new java.io.File(System.getenv("DEFAULT_REPO_KEYFILE"))
    
    Resolver.sftp("Default Repo", host, path).as(user, keyfile)
  }

  val publishTo = untypedResolver
  
  def bigtopProject(name: String, libraries: ModuleID*)(dependencies: Project*) =
    project(name, "bigtop-" + name, new BigtopSubproject(_, libraries: _*), dependencies: _*)

  class BigtopSubproject(info: ProjectInfo, libs: ModuleID*) extends DefaultProject(info) {
    
    override def libraryDependencies =
      super.libraryDependencies ++ libs
    
    val publishTo = untypedResolver
    
    override def packageAction =
      super.packageAction dependsOn { test }

    override def packageToPublishActions =
      super.packageToPublishActions ++ Seq(packageSrc)

  }

}
