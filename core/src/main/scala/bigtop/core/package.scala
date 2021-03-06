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

import scala.xml._

package object core {
  
  implicit def urlToLinkBuilder(url: Url): LinkBuilder =
    LinkBuilder(url)
  
  implicit def pairToUnprefixedAttribute(in: (String, String)): UnprefixedAttribute =
    new UnprefixedAttribute(in._1, in._2, Null)

}