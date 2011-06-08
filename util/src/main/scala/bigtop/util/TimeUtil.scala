package bigtop
package util

import java.util._
import org.joda.time._

object TimeUtil {
  
  def ago(then: Calendar): String =
    ago(then, new GregorianCalendar())
  
  def ago(then: Calendar, now: Calendar): String =
    ago(new DateTime(then), new DateTime(now))

  def ago(then: DateTime): String =
    ago(then, new DateTime())
  
  def ago(then: DateTime, now: DateTime): String = {
    val period = new Period(now, then)
    
    agoInner(period.getYears(), "years", "last year", "next year").
    orElse(agoInner(period.getMonths(), "months", "last month", "next month")).
    orElse(agoInner(period.getWeeks(), "weeks", "last week", "next week")).
    orElse(agoInner(period.getDays(), "days", "yesterday", "tomorrow")).
    orElse(agoInner(period.getHours(), "hours", "one hour ago", "in one hour")).
    orElse(agoInner(period.getMinutes(), "minutes", "one minute ago", "in one minute")).
    orElse(agoInner(period.getSeconds(), "seconds", "one second ago", "in one second")).
    getOrElse("just now")
  }
  
  private def agoInner(amount: => Int, plural: String, last: String, next: String): Option[String] =
    if(amount == 0) {
      None
    } else if(amount == -1) {
      Some(last)
    } else if(amount == 1) {
      Some(next)
    } else if(amount < -1) {
      Some((-amount) + " " + plural + " ago")
    } else {
      Some("in " + amount + " " + plural)
    }
  
}
