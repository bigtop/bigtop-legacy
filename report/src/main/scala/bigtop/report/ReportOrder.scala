package bigtop
package report

import net.liftweb.common.{Box,Empty,Full}

/** A "column" in a report. May or may not explicitly be represented as <td>s in HTML markup. */
case class ReportColumn(val id: String, val label: String) {
  def this(label: String) =
    this(label.trim.
               toLowerCase.
               replaceAll("""[ \t\r\n]+""", "-").
               replaceAll("""[^a-zA-Z0-9-]""", ""),
         label)
}

object ReportColumn {
  def apply(label: String): ReportColumn =
    new ReportColumn(label)
}

/** A possible sort order for a report: combination of a ReportColumn and a direction. */
case class ReportOrder(val col: ReportColumn, val asc: Boolean) {
  lazy val param = col.id + "-" + (if(asc) "asc" else "desc")
  
  def reverse = ReportOrder(col, !asc)
}

trait ReportOrderOps {
  object Asc {
    def apply(col: ReportColumn) =
      ReportOrder(col, true)
    
    def unapply(ord: ReportOrder): Option[ReportColumn] =
      if(ord.asc) Some(ord.col) else None
  }
  
  object Desc {
    def apply(col: ReportColumn) =
      ReportOrder(col, false)
    
    def unapply(ord: ReportOrder): Option[ReportColumn] =
      if(ord.asc) None else Some(ord.col)
  }
}

object ReportOrder extends ReportOrderOps {
 
  val ReportOrderRegex = """^(.*)-(asc|desc)$""".r

  def fromParam(param: Box[String], cols: List[ReportColumn]): Option[ReportOrder] =
    param match {
      case Full(ReportOrderRegex(id, asc)) =>
        cols.find(_.id == id).
             map(col => ReportOrder(col, asc == "asc"))
        
      case _ => None
    }
}