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
package routes

import org.specs._

class ArgSpec extends Specification {
  
  "IntArg.encode works as expected" in {
    IntArg.encode(123) mustEqual "123"
  }

  "IntArg.canDecode works as expected" in {
    IntArg.canDecode("123") mustEqual true
    IntArg.canDecode("abc") mustEqual false
  }
  
  "IntArg.decode decodes integers correctly" in {
    IntArg.decode("123") must beSome(123)
  }

  "StringArg.encode does not (un)escape reserved characters" in {
    StringArg.encode("a/b") mustEqual "a/b"
    StringArg.encode("a%2Fb") mustEqual "a%2Fb"
  }
  
  "StringArg.decode does not (un)escape reserved characters" in {
    StringArg.decode("a/b") must beSome("a/b")
    StringArg.decode("a%2Fb") must beSome("a%2Fb")
  }
  
}
