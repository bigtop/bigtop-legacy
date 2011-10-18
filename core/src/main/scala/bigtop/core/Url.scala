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
package core

import java.net._

import net.liftweb.common.Box
import net.liftweb.http.S

/**
 * Immutable data structure for quick assembly and querying of URLs.
 * Can be used to represent complete or partial URLs. For example:
 *
 * {{{
 * Url(path = "a" :: "b" :: "c" :: Nil).toString       // ==> "/a/b/c"
 * Url(host = "a", path = "b" :: "c" :: Nil).toString  // ==> "//a/b/c"
 * Url(scheme = "http").host("example.com").toString   // ==> "http://example.com"
 * }}}
 * 
 * All fields are stored in unencoded form and are encoded to URL-safe variants when toString is called. For example:
 *
 * {{{
 * Url(path = "a/b" :: "c/d" :: Nil).toString          // ==> "/a%2Fb/c%2Fd"
 * }}}
 */
case class Url(
  /**
   * The unencoded URL scheme, e.g. `"http"` or `"https"`.
   *
   * Defaults to `None`.
   */
  val scheme: Option[String] = None,
  /**
   * The unencoded username and/pr password, e.g. `"user:pass"` in `"http://user:pass;example.com"`.
   *
   * Defaults to `None`.
   */
  val user: Option[String] = None,
  /**
   * The unencoded host name, e.g. "example.com".
   *
   * Defaults to `None`.
   */
  val host: Option[String] = None,
  /**
   * The port, e.g. 80.
   *
   * Note that the URL structure does not understand "default" ports.
   * If you supply a scheme of "https" and a port of 443, for example,
   * the 443 will still appear in the rendered URL string.
   *
   * Defaults to `None`.
   */
  val port: Option[Int] = None,
  /**
   * Is the path absolute?
   *
   * Useful in URLs that have no scheme, host and port. For example:
   *
   * {{{
   * Url(pathAbsolute = false, path = "a" :: "b" :: "c" :: Nil).toString // ==> "a/b/c"
   * Url(pathAbsolute = true, path = "a" :: "b" :: "c" :: Nil).toString  // ==> "/a/b/c"
   * }}}
   *
   * Defaults to `true`.
   */
  val pathAbsolute: Boolean = true,
  /**
   * The unencoded path part of the URL (directory and filename), e.g. `"a" :: "b" :: "c" :: Nil` in `http://example.com/a/b/c"`.*/
  val path: List[String] = Nil,
  /** 
   * Unencoded key/value pairs from the query string, e.g. `("key1" -> "value1") :: ("key2" -> "value2") :: Nil` in `"http://example.com?key1=value1&key2=value2"`.
   *
   * Key/value pairs are stored in the order they appear in the URL. Duplicate keys are allowed.
   *
   * Defaults to `Nil`.
   */
  val query: List[(String, String)] = Nil,
  /**
   * The unencoded "hash" part of the URL, e.g. `"hash"` in `"http://example.com#hash"`.
   *
   * Defaults to `None`.
   */
  val fragment: Option[String] = None
) {
  
  /** Return a new URL with the specified HTTP scheme. */
  def scheme(newScheme: Option[String]): Url =
    copy(scheme = newScheme)
    
  /** Return a new URL with the specified user/password. */
  def user(newUser: Option[String]): Url =
    copy(user = newUser)
    
  /** Return a new URL with the specified host name. */
  def host(newHost: Option[String]): Url =
    copy(host = newHost, pathAbsolute = if(newHost.isDefined) !path.isEmpty else pathAbsolute)
    
  /** Return a new URL with the specified port. */
  def port(newPort: Option[Int]): Url =
    copy(port = newPort)
  
  /** Return a new URL with the specified path. */
  def path(newPath: List[String]): Url =
    copy(path = newPath, pathAbsolute = if(host.isDefined) !newPath.isEmpty else pathAbsolute)

  /** Return a new URL with the specified query parameters. */
  def query(newQuery: List[(String, String)]): Url =
    copy(query = newQuery)
  
  /** Return a list of values for the specified key in the query string. */
  def get(key: String): List[String] =
    query.filter(_._1 == key).map(_._2)
  
  /** Return the first value for the specified key in the query string. */
  def getOne(key: String): Option[String] =
    get(key).headOption
  
  /**
   * Return a new URL with the supplied key set to the supplied values.
   *
   * Any existing instances of `key` are removed from the URL.
   * New key/value pairs are appended to the end of the query string in the order supplied.
   */
  def set(key: String, values: String*): Url =
    query(query.filterNot(_._1 == key) ::: values.toList.map(key -> _))

  /** Return a new URL with all instances of the supplied key removed from the query string. */
  def remove(key: String): Url =
    query(query.filterNot(pair => pair._1 == key))
  
  /** Return a new URL with supplied fragment. */
  def fragment(newFragment: Option[String]): Url =
    copy(fragment = newFragment)
  
  /**
   * Return a new `java.net.URL` representing the same location as this object.
   *
   * @throws java.util.MalformedURLException if this object does not represent an absolute URL.
   */
  def toURL: URL =
    new URL(toString)
  
  // URL serialization code stolen from net/url in Racket:

  /** Returns a string version of this URL using `"&"` to separate query string pairs. */
  override def toString: String =
    toString("&")

  /** Returns a string version of this URL using the supplied string to separate query string pairs. */
  def toString(querySeparator: String): String =
    scheme.map(_ + ":").getOrElse("") +
    (if(user.isDefined || host.isDefined || port.isDefined) {
      "//" + user.map(userInfoEncode(_) + "@").getOrElse("") +
             host.getOrElse("") +
             port.map(":" + _.toString).getOrElse("")
    } else if(scheme == Some("file")) { // always need a "//" for file URLs
      "//"
    } else {
      ""
    }) +
    (if(pathAbsolute && !path.isEmpty) "/" else "") +
    path.map(Url.urlEncode(_)).mkString("/") +
    (if(query.isEmpty) "" else "?" + (query.map(queryEncode _)).mkString(querySeparator)) +
    fragment.map("#" + Url.urlEncode(_)).getOrElse("")
  
  private def userInfoEncode(info: String): String = {
    val pos = info.indexOf(":")
    if(pos < 0) {
      Url.urlEncode(info)
    } else {
      Url.urlEncode(info.substring(0, pos)) + ":" + Url.urlEncode(info.substring(pos + 1))
    }
  }
  
  private def queryEncode(pair: (String, String)): String =
    Url.urlEncode(pair._1) + "=" + Url.urlEncode(pair._2)
  
  /** Returns a case-class-style string representation of this object. Useful for debugging. */
  def toDebugString =
    super.toString
  
}

object Url {
  
  /**
   * Parse the supplied URL string and return a new `Url` object.
   *
   * @throws MalformedURLException if the string could not be parsed.
   * @see Url.fromString a variant of this method that returns an `Option[Url]`
   */
  def apply(url: String): Url =
    fromString(url).getOrElse(throw new MalformedURLException("Could not parse URL: " + url))
  
  // URL parsing code stolen from Racket --------
  
  /**
   * Regular expression matching any absolute or relative URL that is parseable by `apply()` or `fromString()`.
   *
   * @see Url.apply
   * @see Url.fromString
   */
  val UrlRegex =
    ("^" +
     "(?:" +              // / scheme-colon-opt
     "([^:/?#]*)" +       // | #1 = scheme-opt
     ":)?" +              // \
     "(?://" +            // / slash-slash-authority-opt
     "(?:" +              // | / user-at-opt
     "([^/?#@]*)" +       // | | #2 = user-opt
     "@)?" +              // | \
     "([^/?#:]*)?" +      // | #3 = host-opt
     "(?::" +             // | / colon-port-opt
     "([0-9]*)" +         // | | #4 = port-opt
     ")?" +               // | \
     ")?" +               // \
     "([^?#]*)" +         // #5 = path
     "(?:\\?" +           // / question-query-opt
     "([^#]*)" +          // | #6 = query-opt
     ")?" +               // \
     "(?:#" +             // / hash-fragment-opt
     "(.*)" +             // | #7 = fragment-opt
     ")?" +               // \
     "$").r
  
  /**
   * Attempts to parse the supplied `String` into a `Url`.
   *
   * @return `Some[Url]`, or `None` if parsing failed.
   *
   * @see Url.apply a variant of this method that returns a straight `Url` (and throws an exception on failure)
   */
  implicit def fromString(str: String): Option[Url] =
    str match {
      case UrlRegex(scheme, user, host, port, path, query, fragment) =>
        Some(Url(Option(scheme).map(_.toLowerCase),
                 Option(user).map(userInfoDecode(_)),
                 Option(host).map(_.toLowerCase),
                 Option(port).map(_.toInt),
                 Option(path).map(_.startsWith("/")).getOrElse(true),
                 parsePath(Option(path)),
                 parseQuery(Option(query)),
                 Option(fragment).map(urlDecode(_))))
      case _ => None
    }
  
  /** Parses a Java `URL` into a `Url`. */
  implicit def fromJavaURL(url: URL): Url =
    Url(Option(url.getProtocol),
        Option(url.getUserInfo),
        Option(url.getHost),
        if(url.getPort == -1) None else Some(url.getPort),
        Option(url.getPath).map(_.startsWith("/")).getOrElse(true),
        parsePath(Option(url.getPath)),
        parseQuery(Option(url.getQuery)),
        Option(url.getRef))
  
  // Lift-specific stuff ------------------------
  
  /** The current ansolute URL in a Lift application. */
  def liftUrl: Url =
    Url(net.liftweb.http.S.request.open_!.uri)

  /** The current path and query string in a Lift application. */
  def liftPathAndQuery: Box[Url] =
    S.request.
      // get the full path, including the servlet context path:
      map(_.request.uri).
      // append the query string:
      map(p => S.queryString.map(q => p + "?" + q).openOr(p)).
      // parse into a Url:
      map(Url(_))
  
  // Helpers ------------------------------------
  
  private def urlDecode(str: String): String =
    URLDecoder.decode(str, "utf-8")
  
  private def urlEncode(str: String): String =
    URLEncoder.encode(str, "utf-8")
  
  private def userInfoDecode(info: String): String = {
    val pos = info.indexOf(":")
    if(pos == -1) {
      urlDecode(info)
    } else {
      urlDecode(info.substring(0, pos)) + ":" + urlDecode(info.substring(pos + 1))
    }
  }
  
  private def queryDecode(str: String): Option[(String, String)] = {
    val pos = math.min(str.indexOf("&"), str.indexOf(";"))
    if(pos == -1) {
      None
    } else {
      Some((urlDecode(str.substring(0, pos)),
            urlDecode(str.substring(pos + 1))))
    }
  }
  
  private def parsePath(path: Option[String]): List[String] =
    path match {
      case None => Nil
      case Some("") => Nil
      case Some("/") => List("")
      case Some(path) =>
        // Ignore the leading / if there is one - this is dealt with by pathAbsolute:
        val toSplit = if(path.startsWith("/")) path.substring(1) else path

        val split = toSplit.split("/").toList.map(urlDecode _)
        
        // "a/b/".split("/") returns Array("a", "b"), not Array("a", "b", "") as one might expect.
        // to get around this, we re-add the expected blank string to the end of the list:
        if(toSplit.endsWith("/")) split :+ "" else split
    }
  
  private val QueryListRegex = "^([^&;]*)([&;].*)?$".r
  private val QueryItemRegex = "^([^=]*)(=.*)?$".r
  
  private def parseQuery(query: Option[String]): List[(String, String)] =
    query match {
      case Some(QueryListRegex(item, rest)) =>
        item match {
          case QueryItemRegex(key, value) =>
            (urlDecode(key), Option(value).map(_.substring(1)).map(urlDecode(_)).getOrElse("")) :: 
            (if(rest == null) Nil else parseQuery(Some(rest.substring(1))))
        }
      case _ => Nil
    }
  
}
