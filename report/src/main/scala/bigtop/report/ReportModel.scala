package bigtop
package report

/** A "column" in a report. May or may not explicitly be represented as <td>s in HTML markup. */
case class ReportColumn(val name: String)

/** A possible sort order for a report: combination of a ReportColumn and a direction. */
case class ReportOrder(col: ReportColumn, asc: Boolean) {
  lazy val name = col.name + "-" + (if(asc) "asc" else "desc")

  def reverse = ReportOrder(col, !asc)
}

object ReportOrder {
  
  val regex = """^(.*)-(asc|desc)$""".r

  def fromName(name: String): Option[ReportOrder] =
    regex.findFirstMatchIn(name).
          map(m => ReportOrder(ReportColumn(m.subgroups(0)), 
                               if(m.subgroups(1) == "desc") false else true))
  
}

/** Query abstraction for use in a Report. */
trait ReportModel[T] {

  /**
  * The "columns" on the report.
  *
  * Columns are essentially ways of sorting the report.
  * They may not actually be rendered as columns on the page.
  */
  def reportColumns: List[ReportColumn]
  
  /**
  * The possible ways of sorting the report.
  * 
  * By default, calculated as reportColumns x (asc, desc)
  */
  def reportOrders: List[ReportOrder] =
    reportColumns.flatMap(col => ReportOrder(col, true) :: 
                                 ReportOrder(col, false) :: 
                                 Nil)
  
  /** The items visible on a single page of a report. */
  def reportItems(searchTerm: Option[String],
                  order: Option[ReportOrder],
                  start: Int,
                  count: Int): List[T]
  
  /** The total number of items available in a report. */
  def totalReportItems(searchTerm: Option[String]): Int

}
