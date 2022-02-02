package uz.scala.messenger.domain

import uz.scala.messenger.domain.custom.refinements.{EmailAddress, Nickname, Password}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Decoder, Encoder}

case class UserData(
  nickname: Nickname,
  email: EmailAddress,
  password: Password
)

object UserData {
  implicit val enc: Decoder[UserData] = deriveDecoder[UserData]
  implicit val dec: Encoder[UserData] = deriveEncoder[UserData]
}
