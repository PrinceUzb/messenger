package uz.scala.messenger.implicits

import cats.implicits.catsSyntaxOptionId
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.{ResponseCookie, SameSite}

final case class FlashSetting(
  name: String = "HTTP4S_FLASH",
  content: NonEmptyString,
  secure: Boolean = false,
  httpOnly: Boolean = true,
  domain: Option[String] = None,
  path: Option[String] = "/".some,
  sameSite: Option[SameSite] = org.http4s.SameSite.Lax.some,
  extension: Option[String] = None
) {

  def toCookie: ResponseCookie =
    ResponseCookie(
      name = name,
      content = content,
      domain = domain,
      path = path,
      sameSite = sameSite,
      secure = secure,
      httpOnly = httpOnly,
      extension = extension
    )
}
