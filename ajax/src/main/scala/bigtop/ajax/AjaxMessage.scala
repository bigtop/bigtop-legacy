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
package ajax

import blueeyes.core.http._
import blueeyes.core.service._
import java.util.UUID

trait AjaxMessage

/** Register a function to be called later. */
case class Register(pageUuid: UUID, funcUuid: UUID, fn: HttpRequestHandler2[_, _]) extends AjaxMessage

/** Invoke a previously stored function passing in the supplied request. */
case class Invoke(pageUuid: UUID, funcUuid: UUID, req: HttpRequest[_]) extends AjaxMessage

/** Keepalive signal - reset the life count for the specified page. */
case class Heartbeat(pageUuid: UUID) extends AjaxMessage

/** Clock tick - decrement the life count of all pages. */
case object Tick extends AjaxMessage
