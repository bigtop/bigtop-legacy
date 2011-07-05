package bigtop
package core

import java.net._

import net.liftweb.common.Box
import net.liftweb.http.S

case class Url(
  val scheme: Option[String] = None,
  val user: Option[String] = None,
  val host: Option[String] = None,
  val port: Option[Int] = None,
  val pathAbsolute: Boolean = true,
  val path: List[String] = Nil,
  val query: List[(String, String)] = Nil,
  val fragment: Option[String] = None
) {
  
  def scheme(newScheme: Option[String]): Url =
    copy(scheme = newScheme)
    
  def user(newUser: Option[String]): Url =
    copy(user = newUser)
    
  def host(newHost: Option[String]): Url =
    copy(host = newHost, pathAbsolute = if(newHost.isDefined) !path.isEmpty else pathAbsolute)
    
  def port(newPort: Option[Int]): Url =
    copy(port = newPort)
  
  def path(newPath: List[String]): Url =
    copy(path = newPath, pathAbsolute = if(host.isDefined) !newPath.isEmpty else pathAbsolute)

  def query(newQuery: List[(String, String)]): Url =
    copy(query = newQuery)
  
  def get(key: String): List[String] =
    query.filter(_._1 == key).map(_._2)
  
  def getOne(key: String): Option[String] =
    get(key).headOption
  
  def set(key: String, values: String*): Url =
    query(query.filterNot(_._1 == key) ::: values.toList.map(key -> _))

  def remove(key: String): Url =
    query(query.filterNot(pair => pair._1 == key))
  
  def fragment(newFragment: Option[String]): Url =
    copy(fragment = newFragment)
  
  def toURL: URL =
    new URL(urlString)
  
  // URL serialization code stolen from net/url in Racket:
  
  def urlString: String = urlString("&")

  def urlString(pathSeparator: String): String =
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
    (if(query.isEmpty) "" else "?" + (query.map(queryEncode _)).mkString(pathSeparator)) +
    fragment.map("#" + Url.urlEncode(_)).getOrElse("")
  
  def userInfoEncode(info: String): String = {
    val pos = info.indexOf(":")
    if(pos < 0) {
      Url.urlEncode(info)
    } else {
      Url.urlEncode(info.substring(0, pos)) + ":" + Url.urlEncode(info.substring(pos + 1))
    }
  }
  
  def queryEncode(pair: (String, String)): String =
    Url.urlEncode(pair._1) + "=" + Url.urlEncode(pair._2)
  
}

object Url {
  
  def apply(url: String): Url =
    fromString(url).getOrElse(throw new MalformedURLException("Could not parse URL: " + url))
  
  def fromLift: Url =
    Url(net.liftweb.http.S.request.open_!.uri)
  
  // URL parsing code stolen from net/url in Racket:
  
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
  
  implicit def fromJavaURL(url: URL): Url =
    Url(Option(url.getProtocol),
        Option(url.getUserInfo),
        Option(url.getHost),
        if(url.getPort == -1) None else Some(url.getPort),
        Option(url.getPath).map(_.startsWith("/")).getOrElse(true),
        parsePath(Option(url.getPath)),
        parseQuery(Option(url.getQuery)),
        Option(url.getRef))
  
  def currentPathAndQuery: Box[Url] =
    S.request.
      // get the full path, including the servlet context path:
      map(_.request.uri).
      // append the query string:
      map(p => S.queryString.map(q => p + "?" + q).openOr(p)).
      // parse into a Url:
      map(Url(_))
  
  def urlDecode(str: String): String =
    URLDecoder.decode(str, "utf-8")
  
  def urlEncode(str: String): String =
    URLEncoder.encode(str, "utf-8")
  
  def userInfoDecode(info: String): String = {
    val pos = info.indexOf(":")
    if(pos == -1) {
      urlDecode(info)
    } else {
      urlDecode(info.substring(0, pos)) + ":" + urlDecode(info.substring(pos + 1))
    }
  }
  
  def queryDecode(str: String): Option[(String, String)] = {
    val pos = math.min(str.indexOf("&"), str.indexOf(";"))
    if(pos == -1) {
      None
    } else {
      Some((urlDecode(str.substring(0, pos)),
            urlDecode(str.substring(pos + 1))))
    }
  }
  
  def parsePath(path: Option[String]): List[String] =
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
  
  val QueryListRegex = "^([^&;]*)([&;].*)?$".r
  val QueryItemRegex = "^([^=]*)(=.*)?$".r
  
  def parseQuery(query: Option[String]): List[(String, String)] =
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
