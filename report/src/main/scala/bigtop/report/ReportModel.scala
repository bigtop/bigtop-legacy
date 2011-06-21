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
