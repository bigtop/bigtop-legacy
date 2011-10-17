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

import net.liftweb.http.LiftResponse

trait RouteBuilder {
  
  trait PimpedPath {
    type Fn
    type Rt <: Route[_]

    def >>(fn: Fn)(implicit site: Site): Rt
  }
  
  implicit def pimpPath0(path: Path { type Inner = HNil }) =
    new PimpedPath {
      type Fn = () => LiftResponse
      type Rt = Route0
      
      def >>(fn: Fn)(implicit site: Site) =
        Route0(site, path, fn)
    }
  
  implicit def pimpPath1[A](path: Path { type Inner = HCons[A, HNil] }) =
    new PimpedPath {
      type Fn = (A) => LiftResponse
      type Rt = Route1[A]
      
      def >>(fn: Fn)(implicit site: Site) =
        Route1(site, path, fn)
    }
  
  implicit def pimpPath2[A, B](path: Path { type Inner = HCons[A, HCons[B, HNil]] }) =
    new PimpedPath {
      type Fn = (A, B) => LiftResponse
      type Rt = Route2[A, B]

      def >>(fn: Fn)(implicit site: Site) =
        Route2(site, path, fn)
    }
  
  implicit def pimpPath3[A, B, C](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HNil]]] }) =
    new PimpedPath {
      type Fn = (A, B, C) => LiftResponse
      type Rt = Route3[A, B, C]

      def >>(fn: Fn)(implicit site: Site) =
        Route3(site, path, fn)
    }
  
  implicit def pimpPath4[A, B, C, D](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HNil]]]] }) =
    new PimpedPath {
      type Fn = (A, B, C, D) => LiftResponse
      type Rt = Route4[A, B, C, D]

      def >>(fn: Fn)(implicit site: Site) =
        Route4(site, path, fn)
    }
  
  implicit def pimpPath5[A, B, C, D, E](path: Path { type Inner = HCons[A, HCons[B, HCons[C, HCons[D, HCons[E, HNil]]]]] }) =
    new PimpedPath {
      type Fn = (A, B, C, D, E) => LiftResponse
      type Rt = Route5[A, B, C, D, E]

      def >>(fn: Fn)(implicit site: Site) =
        Route5(site, path, fn)
    }

}

object RouteBuilder extends RouteBuilder
