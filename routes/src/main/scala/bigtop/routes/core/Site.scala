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

package bigtop.routes.core

import scala.util.DynamicVariable

trait Site[FrameworkRequest, FrameworkResponse] extends RouteBuilder[FrameworkRequest, FrameworkResponse] {

  /** Reference to this site. Used in RouteBuilder to automatically add routes to the site. */
  implicit val site = this

  /** Programmer-friendly alias for PNil. */
  val end = PNil

  /** Programmer-friendly alias for PAny. */
  val any = PAny
  
  /** Routes in this site in the order they should be visited in appply(). */
  protected var routes: List[Route[_, FrameworkResponse]] = Nil
  
  /**
   * Add a Route to this Site.
   *
   * Return the same Route object that was supplied as an argument.
   */
  protected[routes] def addRoute[Rt <: Route[_, FrameworkResponse]](route: Rt): Rt = {
    routes = routes :+ route
    route
  }
  
  /**
   * Perform a quick check to see if any of the Routes in this Site can respond to the supplied Request.
   *
   * This check skips any expensive computation, so it may return a false positive
   * (i.e. isDefinedAt() returns true but apply() returns None).
   * 
   * However, it should never return a false negative
   * (i.e. isDefinedAt() returns false and apply() returns Some).
   */
  def isDefinedAt(req: FrameworkRequest): Boolean =
    routes.find(_.isDefinedAt(wrapRequest(req))).isDefined
  
  /**
   * Attempt to decode the supplied request and return an HTTP response.
   * 
   * Return None if the request cannot be decoded.
   */
  def apply(frameworkReq: FrameworkRequest): Option[FrameworkResponse] = {
    val req = wrapRequest(frameworkReq)
    routes.find(_.isDefinedAt(req)) match {
      case Some(route) => route.apply(req)
      case None => throw new Exception("Failed to find a matching route for " + req)
    }
  }
  
  /** Convert an implementation-specific request into a routes Request. */
  def wrapRequest(req: FrameworkRequest): Request
  
  /** Prepend a servlet path to path if necessary to reassemble a complete path. */
  def reassemblePath(path: List[String]) =
    path
  
}
