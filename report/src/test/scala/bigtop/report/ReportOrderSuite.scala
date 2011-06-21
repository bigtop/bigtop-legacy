package bigtop
package report

import org.scalatest._

class ReportOrderSuite extends FunSuite with Assertions {
  
  test("ReportColumn(label)") {
    assert(ReportColumn("first name") === ReportColumn("first-name", "first name"))
    assert(ReportColumn("  a  B-c/  ") === ReportColumn("a-b-c", "  a  B-c/  "))
  }
  
  test("Asc constructor and matcher") {
    import ReportOrder._
    
    object Col1 extends ReportColumn("col1", "column 1")
    object Col2 extends ReportColumn("col2", "column 2")
    
    expect(ReportOrder(Col1, true))(Asc(Col1))
    
    expect(true)(ReportOrder(Col1, true) match { case Asc(Col1) => true; case _ => false })
    expect(false)(ReportOrder(Col1, false) match { case Asc(Col1) => true; case _ => false })
    expect(false)(ReportOrder(Col2, true) match { case Asc(Col1) => true; case _ => false })
    expect(false)(ReportOrder(Col2, false) match { case Asc(Col1) => true; case _ => false })
  }
  
  test("Desc constructor and matcher") {
    import ReportOrder._
    
    object Col1 extends ReportColumn("col1", "column 1")
    object Col2 extends ReportColumn("col2", "column 2")
    
    expect(ReportOrder(Col1, false))(Desc(Col1))

    expect(false)(ReportOrder(Col1, true ) match { case Desc(Col1) => true; case _ => false })
    expect(true)(ReportOrder(Col1, false) match { case Desc(Col1) => true; case _ => false })
    expect(false)(ReportOrder(Col2, true ) match { case Desc(Col1) => true; case _ => false })
    expect(false)(ReportOrder(Col2, false) match { case Desc(Col1) => true; case _ => false })
  }
  
}