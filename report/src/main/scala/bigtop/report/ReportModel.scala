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
package report

/** Query abstraction for use in a Report. */
trait ReportModel[T] extends ReportOrderOps {
  
  /**
  * The "columns" on the report.
  *
  * Columns are essentially ways of sorting the report.
  * They may not actually be rendered as columns on the page.
  */
  val reportColumns: List[ReportColumn]
  
  /** The possible ways of sorting the report. */
  private[report] final lazy val reportOrders: List[ReportOrder] =
    reportColumns.flatMap(col => ReportOrder(col, true) :: 
                                 ReportOrder(col, false) :: 
                                 Nil)

  /** The items visible on a single page of a report. */
  def reportItems(searchTerm: Option[String],
                  order: ReportOrder,
                  start: Int,
                  count: Option[Int]): List[T]
  
  /** The total number of items available in a report. */
  def totalReportItems(searchTerm: Option[String]): Int

}
