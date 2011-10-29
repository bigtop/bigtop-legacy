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

package bigtop.routes.blueeyes

import bigtop.core.Url
import bigtop.routes.core._
import bigtop.routes.core.HList._
import java.net.URLEncoder.{encode => urlEncode}

import HListOps._

trait PathBuilder {
  
  def pimp[H <: HList](path: Path { type Inner = H }) =
    path
  
  implicit def pimpPath0(path: Path { type Inner = HNil }) =
    new Path0(path)
  
  implicit def pimpPath1[A](path: Path { type Inner = A :: HNil }) =
    new Path1(path)
  
  implicit def pimpPath2[A, B](path: Path { type Inner = A :: B :: HNil }) =
    new Path2(path)
  
  implicit def pimpPath3[A, B, C](path: Path { type Inner = A :: B :: C :: HNil }) =
    new Path3(path)
  
  implicit def pimpPath4[A, B, C, D](path: Path { type Inner = A :: B :: C :: D :: HNil }) =
    new Path4(path)
  
  implicit def pimpPath5[A, B, C, D, E](path: Path { type Inner = A :: B :: C :: D :: E :: HNil }) =
    new Path5(path)

}

class PathN[Args <: HList](
  val pattern: Path { type Inner = Args }
) {
  
  /** Generate a Url from the supplied HList. */
  private[routes] def url(args: Args): Url =
    Url(path = pattern.encode(args))
  
  /** Generate a URL path string from the supplied HList. */
  private[routes] def path(args: Args): String =
    pattern.encode(args).
            map(urlEncode(_, "utf-8")).
            mkString("/", "/", "")
  
}

case class Path0(
  override val pattern: Path { type Inner = HNil }
) extends PathN[HNil](pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(): Url =
    url(HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(): String =
    path(HNil)

}

case class Path1[A](
  override val pattern: Path { type Inner = A :: HNil }
) extends PathN(pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A): Url =
    url(a :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A): String =
    path(a :: HNil)

}

case class Path2[A, B](
  override val pattern: Path { type Inner = A :: B :: HNil }
) extends PathN(pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B): Url =
    url(a :: b :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B): String =
    path(a :: b :: HNil)

}

case class Path3[A, B, C](
  override val pattern: Path { type Inner = A :: B :: C :: HNil }
) extends PathN(pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C): Url =
    url(a :: b :: c :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C): String =
    path(a :: b :: c :: HNil)

}

case class Path4[A, B, C, D](
  override val pattern: Path { type Inner = A :: B :: C :: D :: HNil }
) extends PathN(pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C, d: D): Url =
    url(a :: b :: c :: d :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C, d: D): String =
    path(a :: b :: c :: d :: HNil)

}

case class Path5[A, B, C, D, E](
  override val pattern: Path { type Inner = A :: B :: C :: D :: E :: HNil }
) extends PathN(pattern) {

  /** Generate a Url from the supplied arguments. */
  def url(a: A, b: B, c: C, d: D, e: E): Url =
    url(a :: b :: c :: d :: e :: HNil)

  /** Generate a URL path string (of the form "/a/b/c") from the supplied arguments. */
  def path(a: A, b: B, c: C, d: D, e: E): String =
    path(a :: b :: c :: d :: e :: HNil)

}
