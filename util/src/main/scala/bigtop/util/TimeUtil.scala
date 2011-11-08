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
package util

import java.util._
import org.joda.time._

/**
 * Time-related utility methods: converting between time formats, temporal arithmetic,
 * formatting times as strings, and so on.
 */
class TimeUtil {
  
  /** Equivalent to `ago(then, Calendar.getInstance)`. */
  def ago(then: Calendar): String =
    ago(then, new GregorianCalendar())
  
  /**
   * Create an informal human-readable string representing the time difference between `then` and `now`.
   *
   * See the docs for tje Joda Time variants for more information. 
   */
  def ago(then: Calendar, now: Calendar): String =
    ago(new DateTime(then), new DateTime(now))

  /** Equivalent to `ago(then, new DateTime)`. */
  def ago(then: DateTime): String =
    ago(then, new DateTime())
  
  /**
   * Create an informal human-readable string representing the time difference between `then` and `now`.
   *
   * Examples:
   *  - 2 years ago   
   *  - last year    
   *  - yesterday     
   *  - in 5 minutes
   */
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
  
  /** Helper that does the bulk of the grunt work for `ago()`. */
  protected def agoInner(amount: => Int, plural: String, last: String, next: String): Option[String] =
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

object TimeUtil extends TimeUtil
