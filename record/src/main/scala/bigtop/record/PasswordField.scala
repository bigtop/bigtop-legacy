package bigtop
package record

import scala.xml.{NodeSeq,Text,UnprefixedAttribute}

import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.http.S
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JE.{JsNull,Str}
import net.liftweb.json._
import net.liftweb.record._
import net.liftweb.record.field._
import net.liftweb.record.field._
import net.liftweb.squerylrecord.SquerylRecordField
import net.liftweb.util.FieldError
import net.liftweb.util.Helpers._

import bigtop.core.BCrypt

object PasswordField {
  @volatile var blankPw = "*******"
  @volatile var minPasswordLength = 5
}

trait BCryptPasswordTypedField extends TypedField[String] with IgnoreInEquals with SquerylRecordField {
  protected var invalidMsg : String = ""

  protected var validatedValue: Box[String] = valueBox
  
  def classOfPersistentField = classOf[String]

  def match_?(candidate: String): Boolean = {
    valueBox.map{BCrypt.compare(candidate, _)}.getOrElse(false)
  }

  override def set_!(in: Box[String]): Box[String] = 
    in match {
      case Full(s) if BCrypt.isHash(s) => in
      case _ => 
        validatedValue = in
        in.map(BCrypt.hash(_))
    }

  def setFromAny(in: Any): Box[String] = genericSetFromAny(in)

  def setFromString(s: String): Box[String] = s match {
    case "" if optional_? => setBox(Empty)
    case _                => setBox(Full(s))
  }

  override def validate: List[FieldError] = runValidation(validatedValue)

  override def notOptionalErrorMessage = S.??("password.must.be.set")

  protected def elem = S.fmapFunc(SFuncHolder(this.setFromAny(_))){
    funcName => <input type="password"
      name={funcName}
      value={valueBox openOr ""}
      tabindex={tabIndex toString}/>}

  def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _ => Full(elem)
    }

  protected def validatePassword(pwdValue: ValueType): List[FieldError] = 
    toBoxMyType(pwdValue) match {
      case Empty|Full(""|null) if !optional_? => Text(notOptionalErrorMessage)
      case Full(s) if s == "*" || s == PasswordField.blankPw || s.length < PasswordField.minPasswordLength => 
        Text(S.??("password.too.short"))
      case _ => Nil
    }

  override def validations = validatePassword _ :: Nil

  def defaultValue = ""

  def asJs = valueBox.map(Str) openOr JsNull

  def asJValue: JValue = valueBox.map(v => JString(v)) openOr (JNothing: JValue)
  def setFromJValue(jvalue: JValue): Box[MyType] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JString(s)                   => setFromString(s)
    case other                        => setBox(FieldHelpers.expectedA("JString", other))
  }
}

class PasswordField[OwnerType <: Record[OwnerType]](rec: OwnerType)
  extends Field[String, OwnerType] with MandatoryTypedField[String] with BCryptPasswordTypedField {

  def this(rec: OwnerType, value: String) = {
    this(rec)
    set(value)
  }

  def owner = rec
}

class OptionalPasswordField[OwnerType <: Record[OwnerType]](rec: OwnerType)
  extends Field[String, OwnerType] with OptionalTypedField[String] with BCryptPasswordTypedField {

  def this(rec: OwnerType, value: Box[String]) = {
    this(rec)
    setBox(value)
  }

  def owner = rec
}

