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

import net.liftweb.common.Box
import net.liftweb.http.{Req, LiftResponse, LiftRules}

trait LiftSite extends Site[Req, LiftResponse] {
    
  /** Lift-compatible DispatchPF function. */
  def dispatchPF: LiftRules.DispatchPF =
    new LiftRules.DispatchPF {
      
      def isDefinedAt(req: Req) =
        LiftSite.this.isDefinedAt(req)
      
      def apply(req: Req) =
        () => Box.option2Box(LiftSite.this.apply(req))
      
    }
  
  def wrapRequest(req: Req): Request =
    new Request { def path = req.path.partPath }
  
}
