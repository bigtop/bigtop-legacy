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

package bigtop.routes
package scalatra

import org.scalatra._

/**
 * Mix this trait into your `ScalatraServlet` to provide a hook to dispatch to a Routes `Site`.
 *
 * @see bigtop.routes.scalatra.Site for an example.
 */
trait BigtopRoutes extends ScalatraKernel {
  
  /** Creates a RouteMatcher from Site. The matcher doesn't add anything to Scalatra's params map. */
  case class SiteMatcher(site: Site) extends RouteMatcher {
    
    def apply(): Option[ScalatraKernel.MultiParams] =
      if(site.isDefinedAt(BigtopRoutes.this)) {
        Some(util.MultiMap())
      } else None
    
  }
  
  /** Version of `ScalatraKernel.get()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def get(site: Site, routeMatchers: RouteMatcher*): Route =
    addRoute(Get, site, routeMatchers)

  /** Version of `ScalatraKernel.post()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def post(site: Site, routeMatchers: RouteMatcher*)(action: => Any): Route =
    addRoute(Post, site, routeMatchers)

  /** Version of `ScalatraKernel.put()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def put(site: Site, routeMatchers: RouteMatcher*)(action: => Any): Route =
    addRoute(Put, site, routeMatchers)

  /** Version of `ScalatraKernel.delete()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def delete(site: Site, routeMatchers: RouteMatcher*)(action: => Any): Route =
    addRoute(Delete, site, routeMatchers)
  
  /** Version of `ScalatraKernel.options()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def options(site: Site, routeMatchers: RouteMatcher*)(action: => Any): Route =
    addRoute(Options, site, routeMatchers)

  /** Version of `ScalatraKernel.patch()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  def patch(site: Site, routeMatchers: RouteMatcher*)(action: => Any): Route =
    addRoute(Patch, site, routeMatchers)

  /** Version of `ScalatraKernel.addRoute()` that dispatches to `Site` if `site.isDefinedAt(request)` returns `true`. */
  protected def addRoute(method: HttpMethod, site: Site, routeMatchers: Iterable[RouteMatcher]): Route =
    addRoute(method, SiteMatcher(site) :: routeMatchers.toList, site.applyOrPass(this))

}