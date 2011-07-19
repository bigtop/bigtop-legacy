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
package snippet

import scala.xml._

object SnippetUtil {
  
  /** Attributes of the first element of the argument. */
  def attrs(in: NodeSeq, skip: List[String] = Nil): MetaData =
    in.headOption.
       map(_.attributes).
       map(skipAttrs(_, skip)).
       getOrElse(scala.xml.Null)
  
  def skipAttrs(attrs: MetaData, skip: List[String]): MetaData =
    if(skip == Nil) {
      attrs
    } else attrs match {
      case PrefixedAttribute(pre, key, value, next) =>
        if(skip.contains(key)) {
          skipAttrs(next, skip)
        } else {
          new PrefixedAttribute(pre, key, value, skipAttrs(next, skip))
        }

      case UnprefixedAttribute(key, value, next) =>
        if(skip.contains(key)) {
          skipAttrs(next, skip)
        } else {
          new UnprefixedAttribute(key, value, skipAttrs(next, skip))
        }

      case Null => Null
    }
  
  /** 
   * Create a nodeseq to nodeseq function that returns a base element
   * adorned with attributes from the input HTML template.
   *
   * For example, calling this method:
   *
   *   def foo = 
   *     "name=username" #> preserveAttributes(SHtml.text(...))
   *
   * as follows:
   *
   *   foo(<input name="username" class="bar"/>)
   *
   * would yield an SHtml.text element with class="bar".
   */
  def preserveAttrs(base: => Elem, skip: List[String] = Nil): (NodeSeq) => NodeSeq =
    (in: NodeSeq) => base % attrs(in, skip)
  
}
