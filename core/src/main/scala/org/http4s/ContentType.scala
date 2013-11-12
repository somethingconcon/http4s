package org.http4s

import Charset._
import java.nio.charset.Charset

//case class ContentType(mediaType: MediaType, params: List[(String, String)] = Nil)
//
//case class MediaType(mainType: String, subtype: String, params: List[(String, String)])




case class ContentTypeRange(mediaRange: MediaRange, charsetRange: CharsetRange = `*`) {
  def value: String = charsetRange match {
    case `*` => mediaRange.value
    case x: Charset => mediaRange.value + "; charset=" + x.value
  }
  def matches(contentType: ContentType) = {
    mediaRange.matches(contentType.mediaType) &&
            ((charsetRange eq `*`) || contentType.definedCharset.map(charsetRange.matches(_)).getOrElse(false))
  }
  override def toString = "ContentTypeRange(" + value + ')'
}

object ContentTypeRange {
  implicit def fromMediaRange(mediaRange: MediaRange): ContentTypeRange = apply(mediaRange)
}

case class ContentType(mediaType: MediaType, definedCharset: Option[Charset]) {
  def value: String = definedCharset match {
    case Some(cs) => mediaType.value + "; charset=" + cs.value
    case _ => mediaType.value
  }

  def withMediaType(mediaType: MediaType) =
    if (mediaType != this.mediaType) copy(mediaType = mediaType) else this
  def withCharset(charset: Charset) =
    if (noCharsetDefined || charset != definedCharset.get) copy(definedCharset = Some(charset)) else this
  def withoutDefinedCharset =
    if (isCharsetDefined) copy(definedCharset = None) else this

  def isCharsetDefined = definedCharset.isDefined
  def noCharsetDefined = definedCharset.isEmpty

  def charset: Charset = definedCharset.getOrElse(`ISO-8859-1`)
}

object ContentType {
  val `text/plain` = ContentType(MediaType.`text/plain`)
  val `application/octet-stream` = ContentType(MediaType.`application/octet-stream`)

  // RFC4627 defines JSON to always be UTF encoded, we always render JSON to UTF-8
  val `application/json` = ContentType(MediaType.`application/json`, `UTF-8`)

  def apply(mediaType: MediaType, charset: Charset): ContentType = apply(mediaType, Some(charset))
  implicit def apply(mediaType: MediaType): ContentType = apply(mediaType, None)
}