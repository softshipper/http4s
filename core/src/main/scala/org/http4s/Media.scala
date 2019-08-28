package org.http4s

import fs2.Stream
import fs2.text.utf8Decode
import org.http4s.headers._
import org.http4s.util.decode

trait Media[F[_]] {
  def body: EntityBody[F]
  def headers: Headers

  final def bodyAsText(implicit defaultCharset: Charset = DefaultCharset): Stream[F, String] =
    charset.getOrElse(defaultCharset) match {
      case Charset.`UTF-8` =>
        // suspect this one is more efficient, though this is superstition
        body.through(utf8Decode)
      case cs =>
        body.through(decode(cs))
    }

  final def contentType: Option[`Content-Type`] =
    headers.get(`Content-Type`)

  final def contentLength: Option[Long] =
    headers.get(`Content-Length`).map(_.length)

  final def charset: Option[Charset] =
    contentType.flatMap(_.charset)
}

object Media {
  def apply[F[_]](b: EntityBody[F], h: Headers): Media[F] = new Media[F] {
    def body = b

    def headers: Headers = h
  }
}
