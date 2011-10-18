/* 
 * Copyright 2011 Untyped Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import sbt.Process._

// A lot of the idioms in this code respectfully stolen from Lift superbuild:
//     https://github.com/lift/superbuild
class BigtopProject(info: ProjectInfo) extends ParentProject(info) {

  // Disable dependencies on sub-projects:
  override def deliverProjectDependencies = Nil

  val untypedRepo = "Untyped" at "http://repo.untyped.com"

  // Libraries ----------------------------------

  val liftVersion = "2.4-M4"
  
  val rogueVersion = "1.0.26"
  
  lazy val bcrypt = "org.mindrot" % "jbcrypt" % "0.3m"
  lazy val c3p0 = "c3p0" % "c3p0" % "0.9.1.2"
  lazy val jetty = "org.mortbay.jetty" % "jetty" % "6.1.22" % "compile"
  lazy val jettyTest = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
  lazy val liftCommon = "net.liftweb" %% "lift-common" % liftVersion % "compile"
  lazy val liftMongodb = "net.liftweb" %% "lift-mongodb" % liftVersion % "compile"
  lazy val liftMongodbRecord = "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile"
  lazy val liftRecord = "net.liftweb" %% "lift-record" % liftVersion % "compile"
  lazy val liftSquerylRecord = "net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile"
  lazy val liftTestkit = "net.liftweb" %% "lift-testkit" % liftVersion % "test"
  lazy val liftWebkit = "net.liftweb" %% "lift-webkit" % liftVersion % "compile"
  lazy val postgresql = "postgresql" % "postgresql" % "8.4-702.jdbc4"
  lazy val rogue = "com.foursquare" %% "rogue" % rogueVersion withSources()
  
  lazy val scalaCheck =
    buildScalaVersion match {
      case "2.8.1" => "org.scala-tools.testing" %% "scalacheck" % "1.8" % "test"
      case "2.8.2" => "org.scala-tools.testing" %% "scalacheck" % "1.8" % "test"
      case _       => "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"
    }
  
  lazy val scalatest =
    buildScalaVersion match {
      case "2.8.1" => "org.scalatest" % "scalatest_2.8.1" % "1.5" % "test"
      case "2.8.2" => "org.scalatest" % "scalatest_2.8.1" % "1.5" % "test"
      case _       => "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
    }
  
  lazy val specs =
    buildScalaVersion match {
      case "2.8.1" => "org.scala-tools.testing" %% "specs" % "1.6.6" % "test"
      case "2.8.2" => "org.scala-tools.testing" %% "specs" % "1.6.6" % "test"
      case _       => "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
    }
  
  // Subprojects --------------------------------
  
  lazy val core = bigtopProject("core", liftCommon, liftWebkit, liftRecord, scalatest, scalaCheck, liftTestkit, jettyTest, bcrypt)()
  lazy val debug = bigtopProject("debug", liftCommon, liftWebkit, scalatest)()
  lazy val record = bigtopProject("record", liftCommon, liftWebkit, liftRecord, liftSquerylRecord, scalatest)(debug, core)
  lazy val report = bigtopProject("report", liftCommon, liftWebkit, scalatest)(debug, core)
  lazy val routes = bigtopProject("routes", liftCommon, liftWebkit, specs, jetty)(debug, core)
  lazy val squeryl = bigtopProject("squeryl", liftCommon, liftWebkit, liftSquerylRecord, postgresql, c3p0, scalatest)(debug, core, record)
  lazy val mongodb = bigtopProject("mongodb", liftCommon, liftWebkit, liftMongodb, liftMongodbRecord, rogue, scalatest)(debug, core, record)
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

  // Subprojects --------------------------------

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
  
  // Documentation ------------------------------
  
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
    override def publishAction        =
      task {
        val src = outputPath / "doc" / "main" / "api"
        val cmd = "rsync -ave ssh " + src.absolutePath + "/ api.bigtopweb.com:api.bigtopweb.com/public/htdocs/" + projectVersion.value
        cmd ! log
        None
      } dependsOn { doc }

    override def makePomAction        = Empty

    // To avoid write collisions with outputDirectories of parent
    override def outputRootPath        = super.outputRootPath        / "apidoc"
    override def managedDependencyPath = super.managedDependencyPath / "apidoc"
   
  	override def documentOptions: Seq[ScaladocOption] =
  		super.documentOptions ++ 
  		Seq(SimpleDocOption("-verbose"),
  		    SimpleDocOption("-doc-source-url"),
      		SimpleDocOption("https://github.com/bigtop/bigtop/tree/master/?{FILE_PATH}"))
  		
  }

}
