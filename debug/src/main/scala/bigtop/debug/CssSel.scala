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
package debug

import text._
import xml._

import net.liftweb.common._
import net.liftweb.util._

/**
 * Helpful utilities for debugging Lift CSS Selectors.
 * See documentation for the companion object for more information.
 */
trait CssSel extends Loggable {
  
  implicit def strToDebugCssBindPromoter(sel: String): CssDebugPromoter =  
    new CssDebugPromoter(Full(sel), CssSelectorParser.parse(sel), cssDebug _)  
  
  implicit def cssSelectorToDebugCssBindPromoter(css: CssSelector): CssDebugPromoter =  
    new CssDebugPromoter(Empty, Full(css), cssDebug _)
  
  def cssDebug(css: Box[String], in: NodeSeq, out: => Seq[NodeSeq]): Seq[NodeSeq] = {
    log(DocText("CssSel ") :: DocText(css.openOr("<no selector>")) :/:
        DocNest(2, DocText("in") :/: 
                   DocNest(4, DocText(in.toString))) :/:
        DocNest(2, DocText("out") :/:
                   DocNest(2, DocText(out.toString))) :/:
        DocNil)

    out
  }
  
  protected def log(doc: text.Document): Unit = {
    val out = new java.io.StringWriter
    doc.format(80, out)
    log(out.toString)
  }
  
  protected def log(str: String): Unit = {
    logger.debug(str)
  }
  
}

/**
 * Helpful utilities for debugging Lift CSS Selectors.
 * 
 * Import this object into your namespace and, anywhere you would normally write "#>",
 * you will also be able to write "#?>" to log the input and output of the transformation.
 *
 * Based on code by David Brooks <https://github.com/junglebarry>.
 * Copyright March 2010 Untyped Ltd.
 */
object CssSel extends CssSel

final class CssDebugPromoter(
  stringSelector: Box[String],
  css: Box[CssSelector],
  cssDebug: (Box[String], NodeSeq, => Seq[NodeSeq]) => Seq[NodeSeq]) {
  
  /** 
   * Inserts a String constant according to the CssSelector rules 
   */  
  def #?>(str: String): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(Text(str)))
    }  
  
  /** 
   * Inserts a NodeSeq constant according to the CssSelector rules 
   */  
  def #?>(ns: NodeSeq): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(ns))
    }

  /** 
   * A function that transforms the content according to the CssSelector rules 
   */  
  def #?>(nsFunc: NodeSeq => NodeSeq): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(nsFunc(in)))
    }  
    
  /** 
   * Inserts a Bindable constant according to the CssSelector rules. 
   * Mapper and Record fields implement Bindable. 
   */  
  def #?>(bindable: Bindable): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(bindable.asHtml))
    }  
  
  /** 
   * Inserts a StringPromotable constant according to the CssSelector rules. 
   * StringPromotable includes Int, Long, Boolean, and Symbol 
   */  
  def #?>(strPromo: StringPromotable): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(Text(strPromo.toString)))
    }  
  
  /** 
   * Applies the N constants according to the CssSelector rules. 
   * This allows for Seq[String], Seq[NodeSeq], Box[String], 
   * Box[NodeSeq], Option[String], Option[NodeSeq] 
   */  
  def #?>(itrConst: IterableConst): CssBind =
    new CssBindImpl(stringSelector, css) {
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, itrConst.constList(in))
    }  
  
  /** 
   * Apply the function and then apply the results account the the CssSelector 
   * rules. 
   * This allows for NodeSeq => Seq[String], NodeSeq =>Seq[NodeSeq], 
   * NodeSeq => Box[String], 
   * NodeSeq => Box[NodeSeq], NodeSeq => Option[String], 
   * NodeSeq =>Option[NodeSeq] 
   */  
  def #?>(itrFunc: IterableFunc): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, itrFunc(in))
    }  
  
  /** 
   * Inserts a String constant according to the CssSelector rules 
   */  
  def replaceWithDebug(str: String): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(Text(str)))
    }  
  
  /** 
   * Inserts a NodeSeq constant according to the CssSelector rules 
   */  
  def replaceWithDebug(ns: NodeSeq): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(ns))
    }  
  
  /** 
   * A function that transforms the content according to the CssSelector rules 
   */  
  def replaceWithDebug(nsFunc: NodeSeq => NodeSeq): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(nsFunc(in)))
    }  
    
  /** 
   * Inserts a Bindable constant according to the CssSelector rules. 
   * Mapper and Record fields implement Bindable. 
   */  
  def replaceWithDebug(bindable: Bindable): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(bindable.asHtml))
    }  
  
  /** 
   * Inserts a StringPromotable constant according to the CssSelector rules. 
   * StringPromotable includes Int, Long, Boolean, and Symbol 
   */  
  def replaceWithDebug(strPromo: StringPromotable): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, List(Text(strPromo.toString)))
    }  
  
  /** 
   * Applies the N constants according to the CssSelector rules. 
   * This allows for Seq[String], Seq[NodeSeq], Box[String], 
   * Box[NodeSeq], Option[String], Option[NodeSeq] 
   */  
  def replaceWithDebug(itrConst: IterableConst): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, itrConst.constList(in))
    }  
  
  /** 
   * Apply the function and then apply the results account the the CssSelector 
   * rules. 
   * This allows for NodeSeq => Seq[String], NodeSeq =>Seq[NodeSeq], 
   * NodeSeq => Box[String], 
   * NodeSeq => Box[NodeSeq], NodeSeq => Option[String], 
   * NodeSeq =>Option[NodeSeq] 
   */  
  def replaceWithDebug(itrFunc: IterableFunc): CssBind =
    new CssBindImpl(stringSelector, css) {  
      def calculate(in: NodeSeq): Seq[NodeSeq] = cssDebug(stringSelector, in, itrFunc(in))
    }
  
}
