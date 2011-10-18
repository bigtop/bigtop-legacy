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

package bigtop.routes.core

import java.net.URLEncoder.{encode => urlEncode}
import java.net.URLDecoder.{decode => urlDecode}

/** A request that can be understood by Routes. Implemented as a wrapper for a web-framework-specific response type. */
trait Request {
  
  /** The path part of the URL, split on "/" characters and URL-decoded. */
  def path: List[String]
  
}

object Request {
  
  // def apply(path: String): Request =
  //   Request(path.split("/").
  //                toList.
  //                filterNot(_.trim == "").
  //                map(urlDecode(_, "utf-8")))
  
  // def createUrl(path: Seq[String]): String =
  //   path.map(urlEncode(_, "utf-8")).mkString("/", "/", "")
  
}
