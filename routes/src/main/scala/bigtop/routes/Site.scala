package bigtop
package routes

import scala.collection._

import net.liftweb.common._
import net.liftweb.http._

trait Site extends HListOps with ArgOps with Function1[Req, Box[LiftResponse]] {

  var routes: List[Route[_]] = Nil
  
  def add[T <: HList](route: Route[T]): Route[T] = {
    routes = routes :+ route
    route
  }
  
  def apply(req: Req): Box[LiftResponse] = 
    dispatch(req, routes)
  
  def dispatch(req: Req, routes: List[Route[_]]): Box[LiftResponse] =
    routes match {
      case Nil => Empty
      
      case head :: tail =>
        head(req) match {
          case Empty => dispatch(req, tail)
          case other => other
        }
    }
}
