package uz.scala.messenger.domain

import cats.Show
import User.UserStatus
import uz.scala.messenger.domain.custom.refinements.{ Password, Tel }
import uz.scala.messenger.types.{ UserId, Username }
import derevo.cats._
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import io.circe.{ Decoder, Encoder }
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import io.circe.refined._
import eu.timepit.refined.cats._

import java.time.ZonedDateTime

@derive(decoder, encoder, show)
case class User(
    id: UserId,
    username: Username,
    phone: Tel,
    createdAt: ZonedDateTime,
    status: UserStatus,
  )

object User {
  @derive(decoder, encoder, show)
  case class CreateUser(
      username: Username,
      phone: Tel,
      password: Password,
    )

  @derive(decoder, encoder)
  case class UserWithPassword(user: User, password: PasswordHash[SCrypt])

  sealed trait UserStatus {
    def value: String = this.toString.toLowerCase
  }

  object UserStatus {
    case object Online extends UserStatus
    case object Offline extends UserStatus

    val statuses: List[UserStatus] = List(Online, Online)

    def find(value: String): Option[UserStatus] =
      statuses.find(_.value == value.toLowerCase)

    def unsafeFrom(value: String): UserStatus =
      find(value).getOrElse(throw new IllegalArgumentException(s"value doesn't match [ $value ]"))

    implicit val enc: Encoder[UserStatus] = Encoder.encodeString.contramap[UserStatus](_.value)
    implicit val dec: Decoder[UserStatus] = Decoder.decodeString.map(unsafeFrom)
    implicit val show: Show[UserStatus] = Show.show(_.value)
  }
}
