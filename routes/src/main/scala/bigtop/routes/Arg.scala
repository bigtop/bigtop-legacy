package bigtop
package routes

trait Arg[T] {

  def apply(value: T): String
  def unapply(path: String): Option[T]
  
  def urlEncode(str: String): String =
    java.net.URLEncoder.encode(str, "utf-8")
  
  def urlDecode(str: String): String =
    java.net.URLDecoder.decode(str, "utf-8")
  
}

object IntArg extends Arg[Int] {
  
  def apply(value: Int) =
    urlEncode(value.toString)
  
  def unapply(path: String) =
    try {
      Some(urlDecode(path).toInt)
    } catch {
      case exn: NumberFormatException => None
    }
  
}

object StringArg extends Arg[String] {
  
  def apply(value: String) =
    urlEncode(value)
  
  def unapply(path: String) =
    Some(urlDecode(path))
  
}
