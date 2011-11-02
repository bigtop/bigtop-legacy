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
package io

import akka.actor.Uuid
import blueeyes.core.data._
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import blueeyes.json.JsonParser
import blueeyes.json.Printer
import scala.util.parsing.combinator._

// Diagnostics ----------------------------------

/** Requests diagnostic information as a JObject. Not exposed via the web. */
case object Status

// Clean-up -------------------------------------

case object Tick

// Handshakes -----------------------------------

case object Handshake

case class HandshakeResponse(
    val sessionId: Uuid,
    val heartbeatTimeout: Int,
    val connectionTimeout: Int,
    val supportedTransports: List[String]
  ) {
  
  override def toString: String =
    List(sessionId,
         heartbeatTimeout,
         connectionTimeout,
         supportedTransports.mkString(",")).mkString(":")
  
}

object HandshakeResponse {
  
  def apply(in: String): Option[HandshakeResponse] =
    in.split(":").toList match {
      case uuid :: heartbeatTimeout :: connectionTimeout :: transports :: Nil =>
        try {
          Some(HandshakeResponse(
                 new Uuid(uuid),
                 heartbeatTimeout.toInt,
                 connectionTimeout.toInt,
                 transports.split(",").toList))
        } catch {
          case exn => None
        }

      case _ => None
    }
    
}

// Session messages -----------------------------

case class ToSession(sessionId: Uuid, msg: Any)

trait IoMessage {
  
  val messageType: Int
  
  def formatJson(data: JValue): String =
    Printer.compact(Printer.render(data))

  def format(messageId: Option[Int], ack: Boolean, endpoint: Option[String], data: Option[String]): String =
    messageType + 
    ":" + messageId.map(_.toString).getOrElse("") + (if(ack) "+" else "") + 
    ":" + endpoint.getOrElse("") + 
    data.map(":" + _).getOrElse("")
  
}

object IoMessage {

  case class Disconnect(
      val endpoint: Option[String] = None
    ) extends IoMessage {

    val messageType = 0

    override def toString =
      format(None, false, endpoint, None)

  }

  case class Connect(
      val endpoint: Option[String] = None,
      val query: List[(String, String)] = Nil
    ) extends IoMessage {

    val messageType = 1

    def formatQuery(query: List[(String, String)]): Option[String] =
      query match {
        case Nil  => None
        case list => Some("?" + list.map { case (k, v) => k + "=" + v }.mkString("&"))
      }

    override def toString =
      format(None, false, endpoint, formatQuery(query))

  }

  case object Heartbeat extends IoMessage {

    val messageType = 2

    override def toString =
      format(None, false, None, None)

  }

  case class Message(
      val messageId: Option[Int] = None,
      val userAck: Boolean = false,
      val endpoint: Option[String] = None,
      val data: Option[String] = None
    ) extends IoMessage {

    val messageType = 3

    override def toString =
      format(messageId, userAck, endpoint, data)

  }

  case class Json(
      val messageId: Option[Int] = None,
      val userAck: Boolean = false,
      val endpoint: Option[String] = None,
      val data: Option[JValue] = None
    ) extends IoMessage {

    val messageType = 4

    override def toString =
      format(messageId, userAck, endpoint, data.map(formatJson _))

  }

  case class Event(
      val messageId: Option[Int] = None,
      val userAck: Boolean = false,
      val endpoint: Option[String] = None,
      val name: String,
      args: List[JValue] = Nil
    ) extends IoMessage {

    val messageType = 5

    override def toString =
      format(messageId, userAck, endpoint, Some(formatJson(("name" -> name) ~ ("args" -> args))))

  }

  case class Ack(
      val messageId: Option[Int] = None,
      val ackId: Int, 
      val args: List[JValue] = Nil
    ) extends IoMessage {

    val messageType = 6

    def formatArgs(args: List[JValue]): String =
      args match {
        case Nil => ""
        case _   => "+" + formatJson(args)
      }

    override def toString =
      format(messageId, false, None, Some(ackId + formatArgs(args)))

  }

  case class Error(
      val endpoint: Option[String] = None,
      val reason: Option[Int] = None,
      val advice: Option[Int] = None
    ) extends IoMessage {

    val messageType = 7

    override def toString =
      format(None, false, endpoint, Some(reason.map(_.toString).getOrElse("") + 
                                         advice.map("+" + _.toString).getOrElse("")))

  }

  case object Noop extends IoMessage {

    val messageType = 8

    override def toString = "8"

  }
  
  object ParseMessage extends RegexParsers {
    
    def unapply(in: String): Option[(Option[Int], Option[Int], Boolean, Option[String], Option[String])] =
      parseAll(message, in).map(Some(_)).getOrElse(None)

    def message: Parser[(Option[Int], Option[Int], Boolean, Option[String], Option[String])] =
      ((messageType ~ messageTypeRest) ^^ { case a ~ b => (a, b._1, b._2, b._3, b._4) })
  
    def messageType: Parser[Option[Int]] =
      (("[0-9]".r) ^^ { case a => Some(a.toInt) })
  
    def messageTypeRest: Parser[(Option[Int], Boolean, Option[String], Option[String])] =
      ((":" ~ messageId ~ ackFlag ~ messageIdRest) ^^ { case _ ~ a ~ b ~ c => (a, b, c._1, c._2) }) |
      (("")                                        ^^ { case _             => (None, false, None, None) })
  
    def messageId: Parser[Option[Int]] =
      (("[0-9]+".r) ^^ { case x => Some(x.toInt) }) |
      (("")         ^^ { case x => None })
  
    def ackFlag: Parser[Boolean] =
      (("+") ^^ { case _ => true }) |
      (("")  ^^ { case _ => false })
  
    def messageIdRest: Parser[(Option[String], Option[String])] =
      ((":" ~ endpoint ~ ":" ~ data) ^^ { case _ ~ a ~ _ ~ b => (a, b) }) |
      ((":" ~ endpoint ~ ":")        ^^ { case _ ~ a ~ _     => (a, None) }) |
      ((":" ~ endpoint)              ^^ { case _ ~ a         => (a, None) }) |
      ((":")                         ^^ { case _             => (None, None) })
  
    def endpoint: Parser[Option[String]] =
      (("[^:]+".r) ^^ { case a => Some(a) }) |
      (("")        ^^ { case _ => None })
    
    def data: Parser[Option[String]] =
      ((".+".r) ^^ { case a => Some(a) }) |
      (("")     ^^ { case _ => None })
  
  }
  
  object ParseConnectQuery extends RegexParsers {

    def apply(in: Option[String]): List[(String, String)] =
      in match {
        case Some(in) => parseAll(data, in).getOrElse(Nil)
        case None     => Nil
      }
    
    def data: Parser[List[(String, String)]] =
      (("?" ~ params) ^^ { case _ ~ a => a })
    
    def params: Parser[List[(String, String)]] =
      (("[^&=]+".r ~ "=" ~ "[^&=]+".r ~ "&" ~ params) ^^ { case k ~ _ ~ v ~ _ ~ p => (k, v) :: p }) |
      (("[^&=]+".r ~ "=" ~ "[^&=]+".r)                ^^ { case k ~ _ ~ v         => (k, v) :: Nil }) |
      (("")                                           ^^ { case _                 => Nil })
    
  }
   
  object ParseAckData extends RegexParsers {
    
    def unapply(in: String): Option[(Int, List[JValue])] =
      parseAll(data, in).map(Some(_)).getOrElse(None)
    
    def data: Parser[(Int, List[JValue])] =
      (("[0-9]+".r ~ "+" ~ args) ^^ { case a ~ _ ~ b => (a.toInt, b) }) |
      (("[0-9]+".r ~ "+")        ^^ { case a ~ _     => (a.toInt, Nil) }) |
      (("[0-9]+".r)              ^^ { case a         => (a.toInt, Nil) })

    def args: Parser[List[JValue]] =
      ((".*".r) ^^ { case a => extractArgs(a) })
    
    def extractArgs(in: String): List[JValue] =
      (for {
        json <- JsonParser.parseOpt(in)
        args <- json -->? classOf[JArray]
      } yield args.elements) getOrElse Nil

  }
  
  object ParseErrorData extends RegexParsers {
    
    def unapply(in: String): Option[(Option[Int], Option[Int])] = {
      parseAll(data, in).map(Some(_)).getOrElse(None)
    }
    
    def data: Parser[(Option[Int], Option[Int])] =
      (("[0-9]+".r ~ "+" ~ ".*".r) ^^ { case a ~ _ ~ b => (Some(a.toInt), Some(b.toInt)) }) |
      (("[0-9]+".r)                ^^ { case a         => (Some(a.toInt), None) }) |
      (("")                        ^^ { case _         => (None, None) })
    
  }
  
  def apply(in: String): Option[IoMessage] =
    unapply(in)
  
  def unapply(in: String): Option[IoMessage] =
    in match {
      case ParseMessage(messageType, messageId, ack, endpoint, data) =>
        messageType match {
          case Some(0) => Some(Disconnect(endpoint))
          case Some(1) => Some(Connect(endpoint, ParseConnectQuery(data)))
          case Some(2) => Some(Heartbeat)
          case Some(3) => Some(Message(messageId, ack, endpoint, data))
          case Some(4) => data.map(data => JsonParser.parseOpt(data)).
                               map(json => Json(messageId, ack, endpoint, json))
          case Some(5) => for {
                            data <- data
                            json <- JsonParser.parseOpt(data)
                            name <- json \ "name" -->? classOf[JString]
                            args <- (json \ "args" -->? classOf[JArray]).map(_.elements).orElse(Some(Nil))
                          } yield {
                            Event(messageId, ack, endpoint, name.value, args)
                          }
          case Some(6) => data match {
                            case Some(ParseAckData(ackId, args)) =>
                              Some(Ack(messageId, ackId, args))
                            case _ => None
                          }
          case Some(7) => data match {
                            case Some(ParseErrorData(reason, advice)) =>
                              Some(Error(endpoint, reason, advice))
                            case None =>
                              Some(Error(endpoint, None, None))
                          }
          case Some(8) => Some(Noop)
          case _       => None
        }
      
      case _ => None
    }

}