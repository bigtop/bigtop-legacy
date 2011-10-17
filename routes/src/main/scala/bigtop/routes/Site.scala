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

package bigtop
package routes

import net.liftweb.http.{Req, LiftResponse}

trait Site extends RequestHandler with RouteBuilder{

  var routes: List[Route[_]] =
    Nil
  
  val end = PNil
  val any = PAny
  
  implicit val site = this
  
  def addRoute(route: Route[_]): Unit =
    routes = routes :+ route
  
  def isDefinedAt(req: Req): Boolean =
    routes.find(_.isDefinedAt(req)).isDefined
  
  def apply(req: Req): LiftResponse = 
    routes.find(_.isDefinedAt(req)) match {
      case Some(route) => route(req)
      case None => throw new Exception("Failed to find a matching route for " + req)
    }
  
}
