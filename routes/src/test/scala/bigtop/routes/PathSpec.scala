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
  
  "Nil-terminated path: decode works as expected" in {
    nilPath.decode(List("abc", "123")) must beNone
    nilPath.decode(List("a", "abc", "b", "123", "c")) must beSome("abc" :: 123 :: HNil)
    nilPath.decode(List("a", "123", "b", "abc", "c")) must beNone
    nilPath.decode(List("c", "123", "b", "abc", "a")) must beNone
  }
  
  "Nil-terminated path: encode works as expected" in {
    nilPath.encode("abc" :: 123 :: HNil) mustEqual List("a", "abc", "b", "123", "c")
  }
   
  val restPath = "a" :/: StringArg :/: "b" :/: IntArg :/: "c" :/: PAny
   
  "Any-terminated path: decode works as expected" in {
    restPath.decode(List("abc", "123")) must beNone
    restPath.decode(List("a", "abc", "b", "123", "c")) must beSome("abc" :: 123 :: List() :: HNil)
    restPath.decode(List("a", "abc", "b", "123", "c", "d", "e")) must beSome("abc" :: 123 :: List("d", "e") :: HNil)
    restPath.decode(List("a", "123", "b", "abc", "c")) must beNone
    restPath.decode(List("c", "123", "b", "abc", "a")) must beNone
  }
  
  "Any-terminated path: encode works as expected" in {
    restPath.encode("abc" :: 123 :: List[String]() :: HNil) mustEqual List("a", "abc", "b", "123", "c")
    restPath.encode("abc" :: 123 :: List("d", "e") :: HNil) mustEqual List("a", "abc", "b", "123", "c", "d", "e")
  }
  
}
