package org.http4s
package parser

import org.parboiled.scala._
import BasicRules._
import Charset._

private[parser] trait AcceptCharsetHeader {
  this: Parser with ProtocolParameterRules with CommonActions =>

  def ACCEPT_CHARSET = rule (
    oneOrMore(CharsetRangeDecl, separator = ListSep) ~ EOI ~~> (xs => Header.`Accept-Charset`(xs.head, xs.tail: _*))
  )

  def CharsetRangeDecl = rule (
    CharsetRangeDef ~ optional(CharsetQuality)
  )

  def CharsetRangeDef = rule (
      "*" ~ push(`*`) | Charset ~~> getCharset
  )

  def CharsetQuality = rule {
    ";" ~ "q" ~ "=" ~ QValue  // TODO: support charset quality
  }

}