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

package object lift {
  
  /**
   * Bidirectional map between a segment of a URL path and a typed Scala value.
   *
   * Extend this trait to provide parsing functionality for any type of Scala value.
   *
   * @see bigtop.routes.core.Arg
   */
  type Arg[X] = core.Arg[X]
  
  /**
   * Arg for mapping URL path segments to/from Int values.
   *
   * @see bigtop.routes.core.IntArg
   */
  val IntArg = core.IntArg

  /**
   * Arg for mapping URL path segments to/from Double values.
   *
   * @see bigtop.routes.core.IntArg
   */
  val DoubleArg = core.DoubleArg

  /** Arg for mapping URL path segments to/from String values.
   *
   * @see bigtop.routes.core.IntArg
   */
  val StringArg = core.StringArg
  
}
