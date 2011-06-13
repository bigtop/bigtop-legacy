package bigtop
package report

import scala.xml._

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import bigtop.core._

trait Report[T] {
  
  // Defaults -----------------------------------
  
  val defaultOrder: Option[ReportOrder] = None
  val defaultStart: Int = 0
  val defaultCount: Int = 25
  
  // UrlVars ------------------------------------

  object OrderParam extends UrlParam[Option[ReportOrder]] {
    val paramName = "sort"

    def encodeParam(value: Option[ReportOrder]): Box[String] =
      if(value == defaultOrder) Empty else value.map(ord => ord.name)
      
    def decodeParam(param: Box[String]): Option[ReportOrder] =
      param.flatMap(ReportOrder.fromName(_)).
            filter(reportModel.reportOrders.contains(_)).
            orElse(defaultOrder)
  }

  object searchTerm extends OptionStringUrlRequestVar("q")
  object order extends UrlRequestVar(OrderParam)
  object start extends IntUrlRequestVar("start", defaultStart)
  object count extends IntUrlRequestVar("count", defaultCount)

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
    "data-report-binding=search-field" #> SHtml.ajaxText(searchTerm.is.getOrElse(""), onSearch _)
  
  def bindSortCombo =
    "data-report-binding=sort-combo" #> SHtml.ajaxSelectObj(
      reportModel.reportOrders.map(ord => ord -> ord.name),
      defaultOrder,
      onSort _)
  
  def bindSortLinks =
    reportModel.reportColumns.map { col =>
      ("data-report-binding=sort-" + col.name + " [onclick]") #> SHtml.ajaxInvoke(() => onResort(col))
    }.foldLeft("* ^^" #> "ignored")(_ & _)
  
  def bindPager = {
    val pages = calcPages
    bindPagerPrev(pages) &
    bindPagerNext(pages) &
    bindPagerPages(pages)
  }
    
  /** List of page indices, each of the form (itemIndex, pageIndex), e.g.: (0 -> 0) :: (5 -> 1) :: ... */
  private[report] def calcPages: List[(Int,Int)] =
    0.until(reportModel.totalReportItems(searchTerm.is), count.is).toList.zipWithIndex 
      
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
  
  def bindContent = 
    reportModel.reportItems(searchTerm.is, order.is, start.is, count.is) match {
      case Nil =>
        "data-report-binding=item" #> NodeSeq.Empty

      case items =>
        "data-report-binding=item" #> items.map("*" #> bindItem(_)) &
        "data-report-binding=empty" #> NodeSeq.Empty
    }
  
  def bindItem(item: T): CssSel

  // AJAX handlers ------------------------------
  
  def onSearch(term: String): JsCmd = JsCmds.Alert("onSearch " + term)
  
  def onSort(ord: ReportOrder): JsCmd = JsCmds.Alert("onSort " + ord.toString)
  def onResort(col: ReportColumn): JsCmd = JsCmds.Alert("onReort " + col.toString)
  
  def onPagerNext: JsCmd = JsCmds.Alert("onPagerNext")
  def onPagerPrev: JsCmd = JsCmds.Alert("onPagerPrev")
  def onPagerPage(start: Int): JsCmd = JsCmds.Alert("onPagerPage " + start)

}
