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

class PathSpec extends Specification {
  
  val nilPath = "a" :/: StringArg :/: "b" :/: IntArg :/: "c" :/: PNil
  
  "Nil-terminated path: canDecode works as expected" in {
    nilPath.canDecode(List("abc", "123")) mustEqual false
    nilPath.canDecode(List("a", "abc", "b", "123", "c")) mustEqual true
    nilPath.canDecode(List("a", "123", "b", "abc", "c")) mustEqual false
    nilPath.canDecode(List("c", "123", "b", "abc", "a")) mustEqual false
  }
  
  "Nil-terminated path: decode works as expected" in {
    nilPath.decode(List("a", "abc", "b", "123", "c")) mustEqual Wrapper("abc" :: 123 :: HNil)
  }
    
  "Nil-terminated path: encode works as expected" in {
    nilPath.encode("abc" :: 123 :: HNil) mustEqual List("a", "abc", "b", "123", "c")
  }
   
  val restPath = "a" :/: StringArg :/: "b" :/: IntArg :/: "c" :/: PAny
   
  "Any-terminated path: canDecode works as expected" in {
    restPath.canDecode(List("abc", "123")) mustEqual false
    restPath.canDecode(List("a", "abc", "b", "123", "c")) mustEqual true
    restPath.canDecode(List("a", "abc", "b", "123", "c", "d", "e")) mustEqual true
    restPath.canDecode(List("a", "123", "b", "abc", "c")) mustEqual false
    restPath.canDecode(List("c", "123", "b", "abc", "a")) mustEqual false
  }
  
  "Any-terminated path: decode works as expected" in {
    restPath.decode(List("a", "abc", "b", "123", "c")) mustEqual Wrapper("abc" :: 123 :: List() :: HNil)
    restPath.decode(List("a", "abc", "b", "123", "c", "d", "e")) mustEqual Wrapper("abc" :: 123 :: List("d", "e") :: HNil)
  }
  
  "Any-terminated path: encode works as expected" in {
    restPath.encode("abc" :: 123 :: List[String]() :: HNil) mustEqual List("a", "abc", "b", "123", "c")
    restPath.encode("abc" :: 123 :: List("d", "e") :: HNil) mustEqual List("a", "abc", "b", "123", "c", "d", "e")
  }
    
}
