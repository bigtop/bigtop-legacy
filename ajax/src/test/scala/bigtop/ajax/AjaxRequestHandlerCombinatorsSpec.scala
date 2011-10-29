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

import bigtop.routes.core._
import bigtop.routes.core.HList._
import blueeyes.concurrent.Future
import blueeyes.concurrent.test.FutureMatchers
import blueeyes.core.http._
import blueeyes.core.service._
import blueeyes.core.data.BijectionsIdentity
import org.specs.Specification
import org.specs.matcher.Matchers._

class AjaxRequestHandlerCombinatorsSpec
    extends Specification 
    with AjaxRequestHandlerCombinators 
    with BijectionsIdentity 
    with FutureMatchers {
  
  
  
}