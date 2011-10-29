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

import bigtop.routes.core._
import blueeyes.concurrent.Future
import blueeyes.core.http._
import blueeyes.core.service._
import java.net.URLDecoder.{decode => urlDecode}

trait RoutesRequestHandlerCombinators {

  def path[Args <: HList, T, S](pattern: PathN[Args]) = {
    (handler: Args => HttpRequestHandler2[T, S]) => new HttpRequestHandler2[T, S] {
    
      def isDefinedAt(req: HttpRequest[T]): Boolean =
        req.uri.
            path.
            map(path => pattern.pattern.canDecode(pathToList(path))).
            getOrElse(false)

      def apply(req: HttpRequest[T]): Future[HttpResponse[S]] =
        req.uri.
            path.
            flatMap(path => pattern.pattern.decode(pathToList(path))).
            map(handler(_).apply(req)).
            getOrElse(throw HttpException(
              HttpStatusCodes.InternalServerError,
              "Could not decode URL path, even though canDecode returned true"))

    } 
  }
  
  private def pathToList(path: String): List[String] =
    path.split("/").
         toList.
         map(urlDecode(_, "utf-8").trim).
         filterNot(_ == "")
  
}
