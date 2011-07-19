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
