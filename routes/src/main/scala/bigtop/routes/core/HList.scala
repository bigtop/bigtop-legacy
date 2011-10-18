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

// HList Implementation based on code by Mark Harrah:
// https://github.com/harrah/up

sealed trait HList

final case class HCons[H, T <: HList](val head : H, val tail : T) extends HList {
  def ::[Next](next: Next) =
    HCons(next, this)
}

sealed abstract class HNil extends HList {
  def ::[X](item: X) =
    HCons(item, this)
}

case object HNil extends HNil
