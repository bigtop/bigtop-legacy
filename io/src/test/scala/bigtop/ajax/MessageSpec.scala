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

import blueeyes.json.JsonAST._
import blueeyes.json.JsonDSL._
import org.specs.Specification
import org.specs.matcher._
import org.specs.matcher.Matchers._

class MessageSpec extends Specification {
  
  case class roundTrip(rhs: IoMessage) extends Matcher[String]() {
    def apply(lhs: => String): (Boolean, String, String) =
      if(IoMessage(lhs) != Some(rhs)) {
        (true,
         "decode(" + lhs + " does not equal IoMessage " + rhs + "", 
         "decode(" + lhs + " does not equal IoMessage " + rhs + "")
      } else if(rhs.toString != lhs) {
        (true,
         lhs + " does not equal encode(IoMessage " + rhs + ")", 
         lhs + " does not equal encode(IoMessage " + rhs + ")")
      } else {
        (true, "", "")
      }
  }
  
  "IoMessage en/decoders" should {
    
    import IoMessage._
    
    "en/decode error packet" in {
      "7:::" must roundTrip(IoMessage.Error())
    }
    
    "en/decode error packet with reason" in {
      "7:::0" must 
        roundTrip(IoMessage.Error(reason = Some(0)))
    }

    "en/decode error packet with reason and advice" in {
      "7:::2+0" must 
        roundTrip(IoMessage.Error(reason = Some(2), advice = Some(0)))
    }
  
    "en/decode error packet with endpoint" in {
      "7::/woot" must 
        roundTrip(IoMessage.Error(endpoint = Some("/woot")))
    }
    
    "en/decode ack packet" in {
      "6:::140" must 
        roundTrip(IoMessage.Ack(ackId = 140))
    }
    
    "en/decode ack packet with args" in {
      """6:::12+["woot","wa"]""" must 
        roundTrip(IoMessage.Ack(ackId = 12, args = List(JString("woot"), JString("wa"))))
    }
    
    "en/decode ack packet with bad json" in {
      """6:::1+{"++]""" must 
        roundTrip(IoMessage.Ack(ackId = 1))
    }
    
    
    "en/decode json packet" in {
      "4:::\"2\"" must
        roundTrip(IoMessage.Json(data = Some("2")))
    }
    
    "en/decode json packet with message id and ack data" in {
      """4:1+::{"a":"b"}""" must
        roundTrip(IoMessage.Json(messageId = Some(1), userAck = true, data = Some("a" -> "b")))
    }
    
    "en/decode an event packet" in {
      """5:::{"name":"woot"}""" must
        roundTrip(IoMessage.Event(name = "woot"))
    }
    
    "en/decode an event packet with message id and ack" in {
      """5:1+::{"name":"tobi"}""" must
        roundTrip(IoMessage.Event(messageId = Some(1), userAck = true, name = "tobi"))
    }
    
    "en/decode an event packet with data" in {
      """5:::{"name":"edwald","args":[{"a": "b"},2,"3"]}""" must
        roundTrip(IoMessage.Event(name = "edwald", args = List("a" -> "b", 2, "3")))
    }
    
    "en/decode a message packet" in {
      "3:::woot" must
        roundTrip(IoMessage.Message(data = Some("woot")))
    }
    
    "en/decode a message packet with id and endpoint" in {
      "3:5:/tobi" must
        roundTrip(IoMessage.Message(messageId = Some(5), endpoint = Some("/tobi")))
    }
    
    "en/decode a heartbeat packet" in {
      "2:::" must
        roundTrip(IoMessage.Heartbeat)
    }
    
    "en/decode a connection packet" in {
      "1::/tobi" must
        roundTrip(IoMessage.Connect(endpoint = Some("/tobi"), query = Nil))
    }
    
    "en/decode a connection packet with query string" in {
      "1::/test:?test=1" must
        roundTrip(IoMessage.Connect(endpoint = Some("/test"), query = List("test" -> "1")))
    }
    
    "en/decode a disconnection packet" in {
      "0::/woot" must
        roundTrip(IoMessage.Disconnect(endpoint = Some("/woot")))
    }
    
  }
  
}