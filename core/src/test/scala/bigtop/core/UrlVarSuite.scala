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

import org.scalatest._

import net.liftweb.common._
import net.liftweb.http.{S, LiftSession}
import net.liftweb.mockweb.MockWeb._
import net.liftweb.util._

class UrlVarSuite extends FunSuite with Assertions {
  
  test("string var") {
    object v extends StringUrlRequestVar("v", "default")
    
    testS("http://example.com/a/b/c") {
      expect("default")(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)
      
      v.set("a/b/c")
      expect("a/b/c")(v.is)
      expect("/a/b/c?v=a%2Fb%2Fc")(v.rewriteCurrentUrl.toString)
    }
    
    testS("http://example.com?v=a%2Fb%2Fc") {
      expect("a/b/c")(v.is)
      v.set("abc")
      expect("abc")(v.is)
    }
  }
  
  test("option string var") {
    object v extends OptionStringUrlRequestVar("v")
    
    testS("http://example.com/a/b/c") {
      expect(None)(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)

      v.set(Some("a/b/c"))
      expect(Some("a/b/c"))(v.is)
      expect("/a/b/c?v=a%2Fb%2Fc")(v.rewriteCurrentUrl.toString)
    }
    
    testS("http://example.com?v=a%2Fb%2Fc") {
      expect(Some("a/b/c"))(v.is)
      v.set(None)
      expect(None)(v.is)
    }
  }
  
  test("boolean var") {
    object v extends BooleanUrlRequestVar("v", true)
    
    testS("http://example.com/a/b/c") {
      expect(true)(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)
      
      v.set(false)
      expect(false)(v.is)
      expect("/a/b/c?v=no")(v.rewriteCurrentUrl.toString)
    }
  }
  
  test("int var") {
    object v extends IntUrlRequestVar("v", 123)
    
    testS("http://example.com/a/b/c") {
      expect(123)(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)
      
      v.set(456)
      expect(456)(v.is)
      expect("/a/b/c?v=456")(v.rewriteCurrentUrl.toString)
    }
    
    testS("http://example.com?v=-123") {
      expect(-123)(v.is)
      v.set(123)
      expect(123)(v.is)
    }
  }
  
  test("option int var") {
    object v extends OptionIntUrlRequestVar("v")
    
    testS("http://example.com/a/b/c") {
      expect(None)(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)

      v.set(Some(123))
      expect(Some(123))(v.is)
      expect("/a/b/c?v=123")(v.rewriteCurrentUrl.toString)
    }
    
    testS("http://example.com/a/b/c?v=456") {
      expect(Some(456))(v.is)
      v.set(None)
      expect(None)(v.is)
      expect("/a/b/c")(v.rewriteCurrentUrl.toString)
    }
  }
  
}
