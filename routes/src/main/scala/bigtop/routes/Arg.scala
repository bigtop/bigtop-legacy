package bigtop
package routes

trait Arg[T] {

  def encode(value: T): String
  def decode(path: String): Option[T]
  
  def urlEncode(str: String): String =
    java.net.URLEncoder.encode(str, "utf-8")
  
  def urlDecode(str: String): String =
    java.net.URLDecoder.decode(str, "utf-8")
  
}

object IntArg extends Arg[Int] {
  
  def encode(value: Int) =
    urlEncode(value.toString)
  
  def decode(path: String) =
    try {
      Some(urlDecode(path).toInt)
    } catch {
      case exn: NumberFormatException => None
    }
  
}

object StringArg extends Arg[String] {
  
  def encode(value: String) =
    urlEncode(value)
  
  def decode(path: String) =
    Some(urlDecode(path))
  
}
