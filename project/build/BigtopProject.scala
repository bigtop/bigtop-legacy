import sbt._

class BigtopProject(info: ProjectInfo) extends ParentProject(info) {

  // Libraries ----------------------------------

  val liftVersion = "2.4-M1"
  
  // Lift:
  lazy val liftSquerylRecord = "net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile"
  lazy val liftTestkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  lazy val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"

  // Databases:
  lazy val postgresql = "postgresql" % "postgresql" % "8.4-702.jdbc4"

  // Test frameworks:
  lazy val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"

  // Subprojects --------------------------------

  lazy val debug   = bigtopProject("debug",   liftWebkit, scalatest)()
  lazy val util    = bigtopProject("util",    liftWebkit, scalatest)(debug)

  lazy val routes  = bigtopProject("routes",  liftWebkit, scalatest)(debug, util)

  lazy val squeryl = bigtopProject("squeryl", liftWebkit, liftSquerylRecord, postgresql, scalatest)(debug)
  
  // Helpers ------------------------------------

  lazy val untypedResolver = {
    def OptionOf[T](v: T): Option[T] = {
      if (v == null) 
        None
      else
        Some(v)
    }

    for { host <- OptionOf(System.getenv("DEFAULT_REPO_HOST"))
          path <- OptionOf(System.getenv("DEFAULT_REPO_PATH"))
          user <- OptionOf(System.getenv("DEFAULT_REPO_USER"))
          keyfile <- OptionOf(System.getenv("DEFAULT_REPO_KEYFILE")) 
    } yield Resolver.sftp("Default Repo", host, path).as(user, new java.io.File(keyfile))
  }

  val publishTo = untypedResolver.getOrElse(null)
  
  def bigtopProject(name: String, libraries: ModuleID*)(dependencies: Project*) =
    project(name, "bigtop-" + name, new BigtopSubproject(_, libraries: _*), dependencies: _*)

  class BigtopSubproject(info: ProjectInfo, libs: ModuleID*) extends DefaultProject(info) {
    
    override def libraryDependencies =
      super.libraryDependencies ++ libs
    
    val publishTo = untypedResolver.getOrElse(null)
    
    override def packageAction =
      super.packageAction dependsOn { test }

    override def packageToPublishActions =
      super.packageToPublishActions ++ Seq(packageSrc)

  }

}
