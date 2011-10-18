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

package bigtop.routes
package lift

import net.liftweb.common.Box
import net.liftweb.http.{Req, LiftResponse, LiftRules}

/**
 * A Lift-compatible Routes `Site`. Extend this trait to implement your own bidirectional type-safe
 * URL mappings for Lift. For example:
 *
 * {{{
 * package code
 * 
 * import bigtop.routes.lift._
 * 
 * object Calculator extends Site {
 * 
 *   // Routing table:
 *
 *   val add      = ("add"      :/: IntArg    :/: "to"   :/: IntArg  :/: end) >> (doAdd _)
 *   val repeat   = ("repeat"   :/: StringArg :/: IntArg :/: "times" :/: end) >> (doRepeat _)
 *   val append   = ("append"                                        :/: any) >> (doAppend _)
 *
 *   // Implementation:
 *
 *   def doAdd(a: Int, b: Int): LiftResponse =
 *     // ...
 * 
 *   def doRepeat(a: String, b: Int): LiftResponse =
 *     // ...
 * }
 * }}}
 *
 * Register the `Site` object with `LiftRules.dispatch` in your `bootstrap.liftweb.Boot` class:
 *
 * {{{
 * package bootstrap.liftweb
 * 
 * class Boot {
 *
 *   def boot {
 *     LiftRules.dispatch.append(code.Calculator.dispatchPF)`
 *   }
 *
 * }
 * }}}
 */
trait Site extends core.Site[Req, LiftResponse] {
    
  /**
   * Generate a `DispatchPF` function for use with `LiftRules.dispatch` or `LiftRules.statelessDispatch`. For example:
   *
   * {{{
   * LiftRules.dispatch.append(MySite.dispatchPF)
   * }}}
   */
  lazy val dispatchPF: LiftRules.DispatchPF =
    new LiftRules.DispatchPF {
      
      def isDefinedAt(req: Req) =
        Site.this.isDefinedAt(req)
      
      def apply(req: Req) =
        () => Box.option2Box(Site.this.apply(req))
      
    }

  /**
   * Extract a URL path from the supplied request.
   *
   * By default, Routes matches on the URL path from `Req.path.partPath`.
   * Override this method if you want to use another path instead.
   */
  def requestPath(req: Req): List[String] =
    req.path.partPath
  
  /** Convert a Lift `Req` into a routes-compatible `Request` object. */
  def wrapRequest(req: Req): core.Request =
    new core.Request {
      lazy val path = requestPath(req)
    }
  
}
