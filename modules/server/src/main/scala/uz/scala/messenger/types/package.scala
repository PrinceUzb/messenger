package uz.scala.messenger

import derevo.cats.{ eqv, show }
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import io.circe.refined._
import eu.timepit.refined.cats._
import ciris.refined._
import uz.scala.messenger.utils.ciris.configDecoder

import java.util.UUID
import scala.concurrent.duration.FiniteDuration

package object types {
  @derive(configDecoder, show)
  @newtype case class JwtAccessTokenKeyConfig(secret: NonEmptyString)

  @derive(configDecoder, show)
  @newtype case class PasswordSalt(secret: NonEmptyString)

  @newtype case class TokenExpiration(value: FiniteDuration)

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class UserId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class Username(value: NonEmptyString)

  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)
}
