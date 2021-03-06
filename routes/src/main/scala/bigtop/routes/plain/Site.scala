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
package plain

import java.net.URLDecoder.{decode => urlDecode}
import javax.servlet.http.{HttpServletRequest,HttpServletResponse}
import scala.util.DynamicVariable

/**
 * A `javax.servlet`-compatible Routes `Site`. Extend this trait to implement your own bidirectional type-safe
 * URL mappings for `HttpServletRequest` and `HttpServletResponse`. For example:
 *
 * {{{
 * package code
 * 
 * import bigtop.routes.plain._
 * 
 * class Calculator extends Site {
 * 
 *   // Routing table:
 *
 *   val add      = ("add"      :/: IntArg    :/: "to"   :/: IntArg  :/: end) >> (doAdd _)
 *   val repeat   = ("repeat"   :/: StringArg :/: IntArg :/: "times" :/: end) >> (doRepeat _)
 *
 *   // Implementation:
 *
 *   def doAdd(a: Int, b: Int): Unit =
 *     response.getWriter.print( // ... )
 * 
 *   def doRepeat(a: String, b: Int): Unit =
 *     response.getWriter.print( // ... )
 * 
 * }
 * }}}
 *
 * Use this site within your own `HttpServlet` as follows:
 *
 * {{{
 * class MyServlet extends HttpServlet {
 * 
 *   def service(req: HttpServletRequest, res: HttpServletResponse): Unit =
 *     Calculator.apply(req, res)
 *
 * }
 * }}}
 */
trait Site extends core.Site[HttpServletRequest, Unit] {
  
  /** The current request being serviced by this Site. */
  protected val _request = new DynamicVariable[Option[HttpServletRequest]](None)
  
  /**
   * The current request being serviced by this Site.
   *
   * Undefined outside of the dynamic scope of Site.apply().
   */
  def request = _request.value.get
  
  /**
   * The current request being serviced by this Site.
   *
   * Undefined outside of the dynamic scope of Site.apply().
   */
  protected val _response = new DynamicVariable[Option[HttpServletResponse]](None)
  
  /**
   * The current response being assembled by this Site.
   *
   * Undefined outside of the dynamic scope of Site.apply().
   */
  def response = _response.value.get
  
  /**
   * Stores references to the request and response objects apply(request).
   *
   * Because Java servlets are based on mutation of the response, it's essential to do one of the following:
   *
   *   - store a reference to the response somewhere in the servlet;
   *   - pass a reference to the response around to any method servicing the request.
   *
   * Because Routes does not explicitly pass references to requests or responses around,
   * we have to use the first option above. We store both the request and response and
   * expose them for the dynamic extent of `apply()` via the `request` and `response` methods.
   *
   * This method is therefore the recommended entry point when using Routes in an HttpServlet.
   */
  def apply(req: HttpServletRequest, res: HttpServletResponse): Unit =
    _request.withValue(Some(req)) {
      _response.withValue(Some(res)) {
        apply(req)
      }
    }
  
  /** Extract a URL path from the supplied request. */
  def requestPath(req: HttpServletRequest): List[String] =
    req.getPathInfo.
        split("/").
        toList.
        map(urlDecode(_, "utf-8").trim).
        filterNot(_ == "")

  def servletPath: List[String] =
    request.getServletPath.
            split("/").
            toList.
            map(urlDecode(_, "utf-8").trim).
            filterNot(_ == "")
            
  def contextPath: List[String] =
    request.getContextPath.
            split("/").
            toList.
            map(urlDecode(_, "utf-8").trim).
            filterNot(_ == "")
  
  override def reassemblePath(path: List[String]) =
    contextPath ::: servletPath ::: path
  
  /** Convert a web-framework-specific request object into a routes Request. */
  def wrapRequest(req: HttpServletRequest): core.Request =
    new core.Request {
      lazy val path = requestPath(req)
    }
  
}
