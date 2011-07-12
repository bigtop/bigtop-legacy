package bigtop
package routes

import net.liftweb.common._
import net.liftweb.http._

abstract class Route[Result <: HList] extends Function1[Req, Box[LiftResponse]] {
  def url(args: Result): String
  def apply(r: Req): Box[LiftResponse]
}
