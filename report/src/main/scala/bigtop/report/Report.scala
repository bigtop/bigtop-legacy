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

import scala.xml._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import bigtop.core._

class ReportException(msg: String) extends Exception(msg)

trait Report[T] {

  // URL parameter names ------------------------
  
  /** The URL parameter name for the search term. */
  val searchTermParamName = "q"

  /** The URL parameter name for the sort order. */
  val orderParamName = "sort"

  /** The URL parameter name for the index of first item to show on the report. */
  val startParamName = "start"

  /** The URL parameter name for the count of items to show on a report page. */
  val countParamName = "count"
  
  // URL parameter defaults ---------------------
  
  /**
   * The default item to start on if no URL parameter is specified.
   * Unless overridden, this defaults to 0.
   */
  lazy val defaultStart: Int = 0
  
  /**
   * The default number of items per page if no URL parameter is specified.
   * Unless overridden, this defaults to 0.
   */
  lazy val defaultCount: Option[Int] = Some(25)

  /**
   * The default sort order if no URL parameter is specified.
   * Unless overridden, this defaults to be the first column in ascending order.
   */
  lazy val defaultOrder: ReportOrder = ReportOrder.Asc(reportModel.reportColumns(0))
  
  // UrlVars ------------------------------------

  object OrderParam extends UrlParam[ReportOrder] {
    val paramName = orderParamName

    def encodeParam(value: ReportOrder): Box[String] =
      if(value == defaultOrder) Empty else Full(value.param)
    
    def decodeParam(param: Box[String]): ReportOrder =
      ReportOrder.fromParam(param, reportModel.reportColumns).
                  getOrElse(defaultOrder)
  }

  object CountParam extends UrlParam[Option[Int]] {
    val paramName = countParamName

    def encodeParam(value: Option[Int]): Box[String] =
      if(value == defaultCount) None else value.map(_.toString)
    
    def decodeParam(param: Box[String]): Option[Int] =
      param.flatMap(s => tryo(Integer.parseInt(s))).or(defaultCount)
  }

  object searchTerm extends OptionStringUrlRequestVar(searchTermParamName)
  object order extends UrlRequestVar(OrderParam)
  object start extends IntUrlRequestVar(startParamName, defaultStart)
  object count extends UrlRequestVar(CountParam)
  
  /**
   * The URL used to open this page.
   * We store this in a val at snippet creation time in case we end up rewriting URLs
   * during an AJAX call (which rewrites the /ajax_request/foo URL).
   */
  val reportUrl = Url.liftUrl
  
  def rewriteUrl: Url =
    List(searchTerm, order, start, count).
      foldLeft(reportUrl)((url, urlVar) => urlVar.rewriteUrl(url))

  // Model --------------------------------------
  
  val reportModel: ReportModel[T]
  
  // Rendering ----------------------------------
  
  def render =
    bindControls &
    bindContent
  
  def bindControls =
    bindSearchField &
    bindSortCombo &
    bindSortLinks &
    bindPager
  
  def bindSearchField =
    "data-report-binding=search-field" #>
      SHtml.ajaxText(searchTerm.is.getOrElse(""), onSearch _, "type" -> "search")
  
  def bindSortCombo =
    "data-report-binding=sort-combo" #> SHtml.ajaxSelectObj(
      reportModel.reportOrders.map(ord => ord -> sortComboLabel(ord)),
      Some(order.is),
      onSort _)
  
  def sortComboLabel(ord: ReportOrder): String =
    "Sort by " + ord.col.label + " - " + (if(ord.asc) "ascending" else "descending")
  
  def bindSortLinks =
    reportModel.reportColumns.map { col =>
      ("data-report-binding=sort-" + col.id + " [onclick]") #> SHtml.ajaxInvoke(() => onResort(col))
    }.foldLeft("* ^^" #> "ignored")(_ & _)
  
  def bindPager = {
    val pages = calcPages
    bindPagerPrev(pages) &
    bindPagerNext(pages) &
    bindPagerPages(pages)
  }
    
  /** List of page indices, each of the form (itemIndex, pageIndex), e.g.: (0 -> 0) :: (5 -> 1) :: ... */
  private[report] def calcPages: List[(Int,Int)] =
    count.is match {
      case Some(count) =>
        0.until(reportModel.totalReportItems(searchTerm.is), count).toList.zipWithIndex
      
      // If count is None (i.e. no paging), there's only ever one page:
      case None =>
        List((0, 0))
    }
      
  def bindPagerPrev(pages: List[(Int, Int)]) =
    if(pages.length <= 1) {
      "data-report-binding=pager-prev" #> NodeSeq.Empty
    } else {
      "data-report-binding=pager-prev [onclick]" #> SHtml.ajaxInvoke(onPagerPrev _)
    }
  
  def bindPagerNext(pages: List[(Int, Int)]) =
    if(pages.length <= 1) {
      "data-report-binding=pager-next" #> NodeSeq.Empty
    } else {
      "data-report-binding=pager-next [onclick]" #> SHtml.ajaxInvoke(onPagerNext _)
    }
  
  def bindPagerPages(pages: List[(Int, Int)]) =
    pages match {
      case _ :: Nil =>
        "data-report-binding=pager-page" #> NodeSeq.Empty
        
      case pages =>
        "data-report-binding=pager-page" #> pages.map {
          case (itemIndex, pageIndex) => 
            "* [onclick]" #> SHtml.ajaxInvoke(() => onPagerPage(itemIndex))
        }
    }

  def bindContent: CssSel =
    bindContent(reportModel.reportItems(searchTerm.is, order.is, start.is, count.is))
  
  def bindContent(items: List[T]): CssSel = 
    reportModel.reportItems(searchTerm.is, order.is, start.is, count.is) match {
      case Nil =>
        "data-report-binding=item" #> NodeSeq.Empty

      case items =>
        "data-report-binding=item" #> items.map("*" #> bindItem(_)) &
        "data-report-binding=empty" #> NodeSeq.Empty
    }
  
  def bindItem(item: T): CssSel

  // AJAX handlers ------------------------------
  
  def onSearch(term: String): JsCmd = {
    searchTerm.set(term.trim match {
      case "" => None
      case other => Some(other)
    })
    JsCmds.RedirectTo(rewriteUrl.toString)
  }
  
  def onSort(ord: ReportOrder): JsCmd = {
    order.set(ord)
    JsCmds.RedirectTo(rewriteUrl.toString)
  }
  
  def onResort(Col: ReportColumn): JsCmd =
    onSort(order.is match {
      case ord @ ReportOrder(Col, _) => ord.reverse
      case other => ReportOrder.Asc(Col)
    })
  
  def onPagerNext: JsCmd = JsCmds.Alert("onPagerNext")
  def onPagerPrev: JsCmd = JsCmds.Alert("onPagerPrev")
  def onPagerPage(start: Int): JsCmd = JsCmds.Alert("onPagerPage " + start)

}
