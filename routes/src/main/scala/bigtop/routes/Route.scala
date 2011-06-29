package bigtop
package routes

import net.liftweb.common._
import net.liftweb.http._

trait Route extends Function1[Req, Box[LiftResponse]]
