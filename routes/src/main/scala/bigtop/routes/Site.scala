package bigtop
package routes

import scala.collection._

import net.liftweb.common._
import net.liftweb.http._

trait Site extends HListOps with ArgOps with Function1[Req, Box[LiftResponse]] {

  var routes: HList = HNil
  
  def add[T <: HList](route: Route[T]): Route[T] = {
    routes = HCons(route, routes)
    route
  }
  
  def apply(req: Req): Box[LiftResponse] = Empty
//    dispatch(req, routes)
  
  // def dispatch(req: Req, routes: HList): Box[LiftResponse] =
  //   routes match {
  //     case HNil => Empty
      
  //     case HCons(head, tail) =>
  //       head(req) match {
  //         case Empty => dispatch(req, tail)
  //         case other => other
  //       }
  //   }
}
