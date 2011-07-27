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
package sitemap

import scala.xml._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

import bigtop.record._

class BaseUserRestoreIdentity[T <: BaseUser[T]](
  meta: BaseUserMeta[T],
  nextUrl: () => String = () => S.param("go").openOr("/")
) extends EarlyResponse (
  () => {
    meta.restoreIdentity
    S.notice("Identity restored")
    Full(new RedirectResponse(nextUrl()))
  }
)
