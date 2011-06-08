package bigtop
package util

import java.util.Calendar

import org.joda.time._
import org.joda.time.format._

import org.scalatest._

class TimeUtilSuite extends FunSuite {
  
  val fmt = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
  
  def st(str: String): DateTime =
    fmt.parseDateTime(str)
  
  def ts(time: DateTime): String =
    fmt.print(time)

  def ts(cal: Calendar): String =
    fmt.print(new DateTime(cal))
  
  test("since") {
    expect("2 years ago"   )(TimeUtil.ago(st("2009-01-01 00:00:00"), st("2011-01-01 00:00:00")))
    expect("last year"     )(TimeUtil.ago(st("2010-01-01 00:00:00"), st("2011-01-01 00:00:00")))
    expect("next year"     )(TimeUtil.ago(st("2012-01-01 00:00:00"), st("2011-01-01 00:00:00")))
    expect("in 2 years"    )(TimeUtil.ago(st("2013-01-01 00:00:00"), st("2011-01-01 00:00:00")))
    expect("2 months ago"  )(TimeUtil.ago(st("2011-04-01 00:00:00"), st("2011-06-01 00:00:00")))
    expect("last month"    )(TimeUtil.ago(st("2011-05-01 00:00:00"), st("2011-06-01 00:00:00")))
    expect("next month"    )(TimeUtil.ago(st("2011-07-01 00:00:00"), st("2011-06-01 00:00:00")))
    expect("in 2 months"   )(TimeUtil.ago(st("2011-08-01 00:00:00"), st("2011-06-01 00:00:00")))
    expect("2 days ago"    )(TimeUtil.ago(st("2011-01-04 00:00:00"), st("2011-01-06 00:00:00")))
    expect("yesterday"     )(TimeUtil.ago(st("2011-01-05 00:00:00"), st("2011-01-06 00:00:00")))
    expect("tomorrow"      )(TimeUtil.ago(st("2011-01-07 00:00:00"), st("2011-01-06 00:00:00")))
    expect("in 2 days"     )(TimeUtil.ago(st("2011-01-08 00:00:00"), st("2011-01-06 00:00:00")))
    expect("2 hours ago"   )(TimeUtil.ago(st("2011-01-01 04:00:00"), st("2011-01-01 06:00:00")))
    expect("one hour ago"  )(TimeUtil.ago(st("2011-01-01 05:00:00"), st("2011-01-01 06:00:00")))
    expect("in one hour"   )(TimeUtil.ago(st("2011-01-01 07:00:00"), st("2011-01-01 06:00:00")))
    expect("in 2 hours"    )(TimeUtil.ago(st("2011-01-01 08:00:00"), st("2011-01-01 06:00:00")))
    expect("2 minutes ago" )(TimeUtil.ago(st("2011-01-01 00:04:00"), st("2011-01-01 00:06:00")))
    expect("one minute ago")(TimeUtil.ago(st("2011-01-01 00:05:00"), st("2011-01-01 00:06:00")))
    expect("in one minute" )(TimeUtil.ago(st("2011-01-01 00:07:00"), st("2011-01-01 00:06:00")))
    expect("in 2 minutes"  )(TimeUtil.ago(st("2011-01-01 00:08:00"), st("2011-01-01 00:06:00")))
    expect("2 seconds ago" )(TimeUtil.ago(st("2011-01-01 00:00:04"), st("2011-01-01 00:00:06")))
    expect("one second ago")(TimeUtil.ago(st("2011-01-01 00:00:05"), st("2011-01-01 00:00:06")))
    expect("in one second" )(TimeUtil.ago(st("2011-01-01 00:00:07"), st("2011-01-01 00:00:06")))
    expect("in 2 seconds"  )(TimeUtil.ago(st("2011-01-01 00:00:08"), st("2011-01-01 00:00:06")))
    expect("just now"      )(TimeUtil.ago(st("2011-01-01 00:00:00"), st("2011-01-01 00:00:00")))
  }
  
}