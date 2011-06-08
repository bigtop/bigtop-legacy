import sbt._

class BigtopProject(info: ProjectInfo) extends ParentProject(info) {

  // Libraries ----------------------------------

  val liftVersion = "2.3"
  
  // Lift:
  lazy val liftSquerylRecord = "net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile"
  lazy val liftTestkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  lazy val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"

  // Databases:
  lazy val postgresql = "postgresql" % "postgresql" % "8.4-702.jdbc4"

  // Test frameworks:
  lazy val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

  // Subprojects --------------------------------

  lazy val debug = bigtopProject("debug", liftWebkit, scalatest)()
  lazy val squeryl = bigtopProject("squeryl", liftWebkit, liftSquerylRecord, postgresql, scalatest)(debug)
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
