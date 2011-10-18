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

trait RouteBuilder[FrameworkRequest, FrameworkResponse] {
  
  trait PimpedPath {
    type Fn
    type Rt <: Route[_, FrameworkResponse]

    def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]): Rt
  }
  
  implicit def pimpPath0(path: Path { type Inner = HNil }) =
    new PimpedPath {
      type Fn = () => FrameworkResponse
      type Rt = Route0[FrameworkResponse]
      
      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route0(path, fn))
    }
  
  implicit def pimpPath1[A](path: Path { type Inner = HCons[A, HNil] }) =
    new PimpedPath {
      type Fn = (A) => FrameworkResponse
      type Rt = Route1[A, FrameworkResponse]
      
      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route1(path, fn))
    }
  
  implicit def pimpPath2[A, B](path: Path { type Inner = HCons[A, HCons[B, HNil]] }) =
    new PimpedPath {
      type Fn = (A, B) => FrameworkResponse
      type Rt = Route2[A, B, FrameworkResponse]

      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route2(path, fn))
    }
  
  implicit def pimpPath3[A, B, C](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HNil]]] }) =
    new PimpedPath {
      type Fn = (A, B, C) => FrameworkResponse
      type Rt = Route3[A, B, C, FrameworkResponse]

      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route3(path, fn))
    }
  
  implicit def pimpPath4[A, B, C, D](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] }) =
    new PimpedPath {
      type Fn = (A, B, C, D) => FrameworkResponse
      type Rt = Route4[A, B, C, D, FrameworkResponse]

      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route4(path, fn))
    }
  
  implicit def pimpPath5[A, B, C, D, E](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] }) =
    new PimpedPath {
      type Fn = (A, B, C, D, E) => FrameworkResponse
      type Rt = Route5[A, B, C, D, E, FrameworkResponse]

      def >>(fn: Fn)(implicit site: Site[FrameworkRequest, FrameworkResponse]) =
        site.addRoute(Route5(path, fn))
    }

}
