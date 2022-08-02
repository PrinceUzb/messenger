package uz.scala.messenger

import cats.implicits.toContravariantOps
import cats.{ Eq, Show }
import dev.profunktor.auth.jwt.JwtToken
import io.circe.generic.semiauto.deriveCodec
import io.circe.{ Codec, Decoder, Encoder }
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

import java.time.ZonedDateTime

package object domain {
  implicit val tokenEq: Eq[JwtToken] = Eq.by(_.value)

  implicit val tokenShow: Show[JwtToken] = Show[String].contramap[JwtToken](_.value)

  implicit val tokenCodec: Codec[JwtToken] = deriveCodec

  implicit val javaTimeShow: Show[ZonedDateTime] = Show[String].contramap[ZonedDateTime](_.toString)

  implicit val passwordHashEncoder: Encoder[PasswordHash[SCrypt]] =
    Encoder.encodeString.contramap(_.toString)
  implicit val passwordHashDecoder: Decoder[PasswordHash[SCrypt]] =
    Decoder.decodeString.map(PasswordHash[SCrypt])
}
