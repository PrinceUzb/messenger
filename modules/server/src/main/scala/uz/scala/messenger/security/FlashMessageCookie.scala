package uz.scala.messenger.security

import cats.implicits.catsSyntaxOptionId
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.{HttpDate, ResponseCookie, SameSite}

import java.time.Instant

final case class FlashMessageCookie(
  name: String,
  content: NonEmptyString,
  expiry: Instant,
  secure: Boolean,
  httpOnly: Boolean = true,
  domain: Option[String] = None,
  path: Option[String] = None,
  sameSite: SameSite = org.http4s.SameSite.Lax,
  extension: Option[String] = None
) {

  def toCookie: ResponseCookie = org.http4s.ResponseCookie(
    name = name,
    content = content,
    expires = HttpDate.unsafeFromInstant(expiry).some,
    domain = domain,
    path = path,
    sameSite = sameSite.some,
    secure = secure,
    httpOnly = httpOnly,
    extension = extension
  )
}
