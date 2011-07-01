import sbt._

// A lot of the idioms in this code respectfully stolen form Lift superbuild:
//     https://github.com/lift/superbuild
class BigtopProject(info: ProjectInfo) extends ParentProject(info) {

  // Libraries ----------------------------------

  val liftVersion = "2.4-M2"
  val rogueVersion = "1.0.14-LIFT-2.4-M2"
  
  // Lift:
  lazy val rogue = "com.foursquare" %% "rogue" % rogueVersion withSources()
  lazy val liftCommon = "net.liftweb" %% "lift-common" % liftVersion % "compile"
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

  lazy val core = bigtopProject("core", liftCommon, liftWebkit, scalatest, liftTestkit, jettyTest)()
  lazy val debug = bigtopProject("debug", liftCommon, liftWebkit, scalatest)()
  lazy val report = bigtopProject("report", liftCommon, liftWebkit, scalatest)(debug, core)
  lazy val squeryl = bigtopProject("squeryl", liftCommon, liftWebkit, liftSquerylRecord, postgresql, scalatest)(debug, core)
  lazy val mongodb = bigtopProject("mongodb", liftCommon, liftWebkit, liftMongodb, liftMongodbRecord, rogue, scalatest)(debug, core)
  lazy val util = bigtopProject("util", liftCommon, liftWebkit, scalatest)(debug, core)
  
  lazy val doc = project(".", "doc", new BigtopDocProject(_))
  
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
  
  class BigtopDocProject(info: ProjectInfo) extends DefaultProject(info) {
    
    /** Sibling of this project, that is, other BigtopSubprojects having the same parent. */
    lazy val siblings =
      info.parent.get.projectClosure.flatMap {
        case c: BigtopSubproject => Some(c)
        case _                   => None
      }

    // We modify the parameter that docAction and docTestAction takes instead of modifying the action itself
    override def mainSources  = concatPaths(siblings) { case p: ScalaPaths        => p.mainSources }
    override def testSources  = concatPaths(siblings) { case p: ScalaPaths        => p.testSources }
    override def docClasspath = concatPaths(siblings) { case p: BasicScalaProject => p.docClasspath }

    private def concatPaths[T](s: Seq[T])(f: PartialFunction[T, PathFinder]) = {
      def finder: T => PathFinder = f orElse { case _ => Path.emptyPathFinder }
      (Path.emptyPathFinder /: s) { _ +++ finder(_) }
    }

    // Nothing to compile, package, deliver or publish
    override def compileAction        = Empty
    override def testCompileAction    = Empty

    override def packageAction        = Empty
    override def packageTestAction    = Empty
    override def packageSrcAction     = Empty
    override def packageTestSrcAction = Empty

    override def publishLocalAction   = Empty
    override def deliverLocalAction   = Empty

    override def deliverAction        = Empty
    override def publishAction        = Empty

    override def makePomAction        = Empty

    // To avoid write collisions with outputDirectories of parent
    override def outputRootPath        = super.outputRootPath        / "apidoc"
    override def managedDependencyPath = super.managedDependencyPath / "apidoc"
   
  }

}
