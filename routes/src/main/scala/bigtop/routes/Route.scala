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

import java.net.URLEncoder.{encode => urlEncode}

import HListOps._

class Route[Res <: HList, FrameworkResponse](
  path: Path { type Inner = Res },
  fn: (Res) => FrameworkResponse
) {
  
  def isDefinedAt(req: Request): Boolean =
    path.canDecode(req.path)

  def apply(req: Request): Option[FrameworkResponse] =
    path.decode(req.path).map(fn)
  
  def url(args: Res) =
    path.encode(args).
         map(urlEncode(_, "utf-8")).
         mkString("/", "/", "")

  def apply(args: Res) =
    fn(args)

}

case class Route0[FrameworkResponse](
  val path: Path { type Inner = HNil },
  val fn: () => FrameworkResponse
) extends Route[HNil, FrameworkResponse](path, hlistFunction0(fn)) {

  def url(): String =
    url(HNil)

  def apply() =
    fn()

}

case class Route1[A, FrameworkResponse](
  val path: Path { type Inner = HCons[A, HNil] },
  val fn: (A) => FrameworkResponse
) extends Route(path, hlistFunction1(fn)) {

  def url(a: A): String =
    url(a :: HNil)

  def apply(a: A) =
    fn(a)

}

case class Route2[A, B, FrameworkResponse](
  val path: Path { type Inner = HCons[A, HCons[B, HNil]] },
  val fn: (A, B) => FrameworkResponse
) extends Route(path, hlistFunction2(fn)) {

  def url(a: A, b: B): String =
    url(a :: b :: HNil)

  def apply(a: A, b: B) =
    fn(a, b)

}

case class Route3[A, B, C, FrameworkResponse](
  val path: Path { type Inner = HCons[A, HCons[B, HCons[C, HNil]]] },
  val fn: (A, B, C) => FrameworkResponse
) extends Route(path, hlistFunction3(fn)) {

  def url(a: A, b: B, c: C): String =
    url(a :: b :: c :: HNil)

  def apply(a: A, b: B, c: C) =
    fn(a, b, c)

}

case class Route4[A, B, C, D, FrameworkResponse](
  val path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] },
  val fn: (A, B, C, D) => FrameworkResponse
) extends Route(path, hlistFunction4(fn)) {

  def url(a: A, b: B, c: C, d: D): String =
    url(a :: b :: c :: d :: HNil)

  def apply(a: A, b: B, c: C, d: D) =
    fn(a, b, c, d)

}

case class Route5[A, B, C, D, E, FrameworkResponse](
  val path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] },
  val fn: (A, B, C, D, E) => FrameworkResponse
) extends Route(path, hlistFunction5(fn)) {

  def url(a: A, b: B, c: C, d: D, e: E): String =
    url(a :: b :: c :: d :: e :: HNil)

  def apply(a: A, b: B, c: C, d: D, e: E) =
    fn(a, b, c, d, e)

}
