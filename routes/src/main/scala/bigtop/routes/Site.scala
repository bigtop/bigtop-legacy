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

trait Site[FrameworkRequest, FrameworkResponse] extends RouteBuilder[FrameworkRequest, FrameworkResponse] {
  
  var routes: List[Route[_, FrameworkResponse]] =
    Nil
  
  val end = PNil
  val any = PAny
  
  implicit val site = this
  
  def addRoute[Rt <: Route[_, FrameworkResponse]](route: Rt): Rt = {
    routes = routes :+ route
    route
  }
  
  def isDefinedAt(req: FrameworkRequest): Boolean =
    routes.find(_.isDefinedAt(wrapRequest(req))).isDefined
  
  def apply(frameworkReq: FrameworkRequest): FrameworkResponse = {
    val req = wrapRequest(frameworkReq)
    
    routes.find(_.isDefinedAt(req)) match {
      case Some(route) => route.apply(req)
      case None => throw new Exception("Failed to find a matching route for " + req)
    }
  }
  
  def wrapRequest(req: FrameworkRequest): Request
  
}
