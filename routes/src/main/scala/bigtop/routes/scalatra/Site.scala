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

import java.net.URLDecoder.{decode => urlDecode}
import javax.servlet.http.{HttpServletRequest,HttpServletResponse}
import org.scalatra.ScalatraKernel
import scala.util.DynamicVariable

/**
 * A Scalatra-compatible Routes `Site`. Extend this trait to implement your own bidirectional type-safe
 * URL mappings. For example:
 *
 * {{{
 * package code
 * 
 * import bigtop.routes.scalatra._
 * 
 * object Calculator extends Site[MyServlet] {
 * 
 *   val add    = ("add"    :/: IntArg    :/: "to"   :/: IntArg  :/: end) >> (doAdd _)
 *   val repeat = ("repeat" :/: StringArg :/: IntArg :/: "times" :/: end) >> (doRepeat _)
 *
 *   // Implementation:
 *
 *   def doAdd(a: Int, b: Int): Any =
 *     <html>{ // ... }</html>
 * 
 *   def doRepeat(a: String, b: Int): Any =
 *     <html>{ // ... }</html>
 * 
 * }
 * }}}
 * 
 * Use this site within your `ScalatraServlet` as follows:
 *
 * {{{
 * class MyServlet extends ScalatraServlet with BigtopRoutes {
 *   
 *   // get(...) { ... } etc...
 * 
 *   get(Calculator)   // alternate Site version of get() provided by BigtopRoutes
 *   
 *   // notFound { ... } etc...
 * 
 * }
 * }}}
 *
 * You can access any functionality in your servlet via the `Site.kernel()` method.
 */
trait Site extends core.Site[ScalatraKernel, Any] {
  
  /** The current ScalatraKernel being serviced by this Site. */
  protected val _kernel = new DynamicVariable[Option[ScalatraKernel]](None)
  
  /**
   * The current ScalatraKernel being serviced by this Site.
   *
   * Undefined outside of the dynamic scope of Site.apply().
   */
  def kernel = _kernel.value.get
  
  /** Alias for `kernel.pass`. */
  def pass = kernel.pass
  
  /** Version of apply() that tells kernel to pass to the next Scalatra route if we cannot service the request. */
  def applyOrPass(k: ScalatraKernel): Any =
    apply(k).getOrElse(k.pass)
  
  /**
   * Stores a reference to the `ScalatraKernel` and calls `apply()`.
   */
  override def apply(k: ScalatraKernel): Option[Any] =
    _kernel.withValue(Some(k)) {
      super.apply(k)
    }
  
  /** Extract a URL path from the supplied request. */
  def requestPath(req: HttpServletRequest): List[String] =
    req.getPathInfo.
        split("/").
        toList.
        map(urlDecode(_, "utf-8").trim).
        filterNot(_ == "")


  def servletPath: List[String] =
    kernel.request.
           getServletPath.
           split("/").
           toList.
           map(urlDecode(_, "utf-8").trim).
           filterNot(_ == "")
            
  def contextPath: List[String] =
    kernel.request.
           getContextPath.
           split("/").
           toList.
           map(urlDecode(_, "utf-8").trim).
           filterNot(_ == "")
  
  override def reassemblePath(path: List[String]) =
    contextPath ::: servletPath ::: path
  
  /** Convert a web-framework-specific request object into a routes Request. */
  def wrapRequest(kernel: ScalatraKernel): core.Request =
    new core.Request {
      lazy val path = requestPath(kernel.request)
    }
  
}
