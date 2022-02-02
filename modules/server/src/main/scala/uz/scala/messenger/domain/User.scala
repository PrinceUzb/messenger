package uz.scala.messenger.domain

import uz.scala.messenger.domain.custom.refinements.{EmailAddress, Nickname}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.refined._

import java.time.LocalDateTime
import java.util.UUID

case class User(
  id: UUID,
  nickname: Nickname,
  email: EmailAddress,
  createdAt: LocalDateTime
)

object User {
  implicit val enc: Encoder[User] = deriveEncoder[User]
  implicit val dec: Decoder[User] = deriveDecoder[User]
}
