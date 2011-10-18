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

import bigtop.core.Url
import java.net.URLEncoder.{encode => urlEncode}

import HListOps._

class Route[Res <: HList, FrameworkResponse](
  pattern: Path { type Inner = Res },
  fn: (Res) => FrameworkResponse
) {
  
  /**
   * Perform a quick check to see if this Route can respond to the supplied Request.
   *
   * This check skips any expensive computation, so it may return a false positive
   * (i.e. isDefinedAt() returns true but apply() returns None).
   * 
   * However, it should never return a false negative
   * (i.e. isDefinedAt() returns false and apply() returns Some).
   */
  def isDefinedAt(req: Request): Boolean =
    pattern.canDecode(req.path)
  
  /**
   * Attempt to decode the supplied request and return an HTTP response.
   * 
   * Return None if the request cannot be decoded.
   */
  def apply(req: Request): Option[FrameworkResponse] =
    pattern.decode(req.path).map(fn)
  
  /** Generate a Url from the supplied HList. */
  private[routes] def url(args: Res): Url =
    Url(path = pattern.encode(args))
  
  /** Generate a URL path string from the supplied HList. */
  private[routes] def path(args: Res): String =
    pattern.encode(args).
            map(urlEncode(_, "utf-8")).
            mkString("/", "/", "")
  
  /** Generate an HTTP response from the supplied HList. */
  private[routes] def apply(args: Res) =
    fn(args)

}

case class Route0[FrameworkResponse](
  val pattern: Path { type Inner = HNil },
  val fn: () => FrameworkResponse
) extends Route[HNil, FrameworkResponse](pattern, hlistFunction0(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(): Url =
    url(HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(): String =
    path(HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply() =
    fn()

}

case class Route1[A, FrameworkResponse](
  val pattern: Path { type Inner = HCons[A, HNil] },
  val fn: (A) => FrameworkResponse
) extends Route(pattern, hlistFunction1(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A): Url =
    url(a :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A): String =
    path(a :: HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply(a: A) =
    fn(a)

}

case class Route2[A, B, FrameworkResponse](
  val pattern: Path { type Inner = HCons[A, HCons[B, HNil]] },
  val fn: (A, B) => FrameworkResponse
) extends Route(pattern, hlistFunction2(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B): Url =
    url(a :: b :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B): String =
    path(a :: b :: HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply(a: A, b: B) =
    fn(a, b)

}

case class Route3[A, B, C, FrameworkResponse](
  val pattern: Path { type Inner = HCons[A, HCons[B, HCons[C, HNil]]] },
  val fn: (A, B, C) => FrameworkResponse
) extends Route(pattern, hlistFunction3(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C): Url =
    url(a :: b :: c :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C): String =
    path(a :: b :: c :: HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply(a: A, b: B, c: C) =
    fn(a, b, c)

}

case class Route4[A, B, C, D, FrameworkResponse](
  val pattern: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] },
  val fn: (A, B, C, D) => FrameworkResponse
) extends Route(pattern, hlistFunction4(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C, d: D): Url =
    url(a :: b :: c :: d :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C, d: D): String =
    path(a :: b :: c :: d :: HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply(a: A, b: B, c: C, d: D) =
    fn(a, b, c, d)

}

case class Route5[A, B, C, D, E, FrameworkResponse](
  val pattern: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] },
  val fn: (A, B, C, D, E) => FrameworkResponse
) extends Route(pattern, hlistFunction5(fn)) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C, d: D, e: E): Url =
    url(a :: b :: c :: d :: e :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C, d: D, e: E): String =
    path(a :: b :: c :: d :: e :: HNil)

  /** Generate an HTTP response from the supplied arguments. */
  def apply(a: A, b: B, c: C, d: D, e: E) =
    fn(a, b, c, d, e)

}
