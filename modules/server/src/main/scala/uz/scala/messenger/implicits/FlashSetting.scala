package uz.scala.messenger.implicits

import cats.implicits.catsSyntaxOptionId
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.{HttpDate, ResponseCookie, SameSite}
import uz.scala.messenger.utils.AlertLevel

import java.time.Instant

final case class FlashSetting(
  level: AlertLevel,
  content: NonEmptyString,
  name: String = "HTTP4S_FLASH",
  secure: Boolean = false,
  httpOnly: Boolean = false,
  domain: Option[String] = None,
  path: Option[String] = "/".some,
  extension: Option[String] = None,
  sameSite: Option[SameSite] = org.http4s.SameSite.Lax.some,
) {

  def toCookie: ResponseCookie =
    ResponseCookie(
      name = name,
      content = level.value + "-" + content,
      domain = domain,
//      expires = HttpDate.unsafeFromInstant(Instant.now().plusSeconds(1)).some,
      path = path,
      sameSite = sameSite,
      secure = secure,
      httpOnly = httpOnly,
      extension = extension
    )
}
