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

package bigtop.routes.plain

import java.net.URLDecoder.{decode => urlDecode}
import javax.servlet._
import javax.servlet.http._
import org.specs.matcher.Matcher

/** Helper methods for constructing and matching on Lift requests and responses. */
trait Http {

  case class TestRequest(val path: String) extends HttpServletRequest {

    def getAuthType: String = ""
    def getContextPath: String = ""
    def getCookies: Array[Cookie] = new Array(0)
    def getDateHeader(name: String): Long = 0L
    def getHeader(name: String): String = ""
    def getHeaderNames: java.util.Enumeration[Any] = null
    def getHeaders(name: String): java.util.Enumeration[Any] = null
    def getIntHeader(name: String): Int = 0
    def getMethod: String = ""
    def getPathInfo: String = "" 
    def getPathTranslated: String = path
    def getQueryString: String = ""
    def getRemoteUser: String = ""
    def getRequestedSessionId: String = ""
    def getRequestURI: String = ""
    def getRequestURL: StringBuffer = new StringBuffer("")
    def getServletPath: String = ""
    def getSession: HttpSession = null
    def getSession(create: Boolean): HttpSession = null
    def getUserPrincipal: java.security.Principal = null
    def isRequestedSessionIdFromCookie: Boolean = false
    def isRequestedSessionIdFromUrl: Boolean = false
    def isRequestedSessionIdFromURL: Boolean = false
    def isRequestedSessionIdValid: Boolean = false
    def isUserInRole(role: String): Boolean = false
    
    def getAttribute(name: String): java.lang.Object = null
    def getAttributeNames(): java.util.Enumeration[Any] = null
    def getCharacterEncoding(): String = ""
    def getContentLength(): Int = 0
    def getContentType(): String = ""
    def getInputStream(): ServletInputStream = null
    def getLocalAddr(): String = ""
    def getLocale(): java.util.Locale = null
    def getLocales(): java.util.Enumeration[Any] = null
    def getLocalName(): String = null
    def getLocalPort(): Int = 0
    def getParameter(name: String): String = ""
    def getParameterMap(): java.util.Map[Any, Any] = null
    def getParameterNames(): java.util.Enumeration[Any] = null
    def getParameterValues(name: String): Array[String] = new Array(0)
    def getProtocol(): String = ""
    def getReader(): java.io.BufferedReader = null
    def getRealPath(path: String): String = ""
    def getRemoteAddr(): String = ""
    def getRemoteHost(): String = ""
    def getRemotePort(): Int = 0
    def getRequestDispatcher(path: String): RequestDispatcher = null
    def getScheme(): String = ""
    def getServerName(): String = ""
    def getServerPort(): Int = 0
    def isSecure(): Boolean = false
    def removeAttribute(name: String): Unit = {}
    def setAttribute(name: String, value: java.lang.Object): Unit = {}
    def setCharacterEncoding(env: String): Unit = {}
    
    override def equals(that: Any) =
      that match {
        case that: HttpServletRequest =>
          this.getPathTranslated == that.getPathTranslated
        case _ => false
      }
  }

  class TestResponse extends HttpServletResponse {
    import java.io._
    
    val buffer = new StringWriter
    
    def addCookie(cookie: Cookie): Unit = {}
    def addDateHeader(name: String, date: Long): Unit = {}
    def addHeader(name: String, value: String): Unit = {}
    def addIntHeader(name: String, value: Int): Unit = {}
    def containsHeader(name: String): Boolean = false
    def encodeRedirectUrl(url: String): String = url
    def encodeRedirectURL(url: String): String = url
    def encodeUrl(url: String): String = url
    def encodeURL(url: String): String = url
    def sendError(sc: Int): Unit = {}
    def sendError(sc: Int, msg: String): Unit = {}
    def sendRedirect(location: String): Unit = {}
    def setDateHeader(name: String, date: Long): Unit = {}
    def setHeader(name: String, value: String): Unit = {}
    def setIntHeader(name: String, value: Int): Unit = {}
    def setStatus(sc: Int): Unit = {}
    def setStatus(sc: Int, sm: String): Unit = {}
    
    def flushBuffer: Unit =
      buffer.flush
    
    def getBufferSize =
      buffer.toString.length
    
    def getCharacterEncoding: String = ""
    def getContentType: String = ""
    def getLocale: java.util.Locale = null
    
    def getOutputStream: ServletOutputStream =
      null
    
    def getWriter: PrintWriter =
      new PrintWriter(buffer)
    
    def isCommitted: Boolean = false
    def reset: Unit = {}
    def resetBuffer: Unit = {}
    def setBufferSize(size: Int): Unit = {} 
    def setCharacterEncoding(charset: String): Unit = {}
    def setContentLength(len: Int): Unit = {}
    def setContentType(`type`: String): Unit = {}
    def setLocale(loc: java.util.Locale): Unit = {}
          
    override def equals(that: Any): Boolean =
      that match {
        case that: TestResponse =>
          this.buffer.toString == that.buffer.toString
        case _ => false
      }
    
    override def toString: String =
      "TestResponse(" + buffer.toString + ")"
  }

  // 
  // case class matchResponse(a: LiftResponse) extends Matcher[LiftResponse]() {
  //   def apply(b: => LiftResponse) = {
  //     val matched =
  //       (a, b) match {
  //         case (a: InMemoryResponse, b: InMemoryResponse) =>
  //           a.code == b.code &&
  //           a.headers == b.headers &&
  //           a.cookies == b.cookies &&
  //           new String(b.data, "UTF-8") == new String(b.data, "UTF-8")
  //     
  //         case _ => false
  //       }
  // 
  //     (matched, "responses matched", "responses did not match")
  //   }
  // }
  // 
  // case class matchOptionalResponse(a: Option[LiftResponse]) extends Matcher[Option[LiftResponse]]() {
  //   def apply(b: => Option[LiftResponse]) = {
  //     val matched =
  //       (a, b) match {
  //         case (Some(a: InMemoryResponse), Some(b: InMemoryResponse)) =>
  //           a.code == b.code &&
  //           a.headers == b.headers &&
  //           a.cookies == b.cookies &&
  //           new String(b.data, "UTF-8") == new String(b.data, "UTF-8")
  //     
  //         case _ => false
  //       }
  // 
  //     (matched, "responses matched", "responses did not match")
  //   }
  // }

}
