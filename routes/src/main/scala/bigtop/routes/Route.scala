package bigtop
package routes

import net.liftweb.common._
import net.liftweb.http._

abstract class Route(path: Path) extends Function1[Req, Box[LiftResponse]] {
  def url(args: path.Result): String = path.encode(args).mkString("/")

  def apply(r:Req): Box[LiftResponse]
}

class RoutedPath(path: Path) {
  def >>(f: HListFunction[path.Result, Box[LiftResponse]]): Route = 
    new Route(path) {
      def apply(r:Req): Box[LiftResponse] = 
        path.decode(r.path.partPath).map(f(_)).getOrElse(Empty)
    }
}

trait RouteOps {
  implicit def path2RoutedPath(path: Path): RoutedPath = new RoutedPath(path)
}

object RouteOps extends RouteOps
