package uz.scala.messenger.domain

import cats.effect.Sync
import cats.implicits._
import uz.scala.messenger.domain.custom.refinements.{EmailAddress, Password}
import uz.scala.messenger.domain.custom.utils.MapConvert
import uz.scala.messenger.domain.custom.utils.MapConvert.ValidationResult
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.circe.refined._

case class Credentials(email: EmailAddress, password: Password)
object Credentials {
  implicit val dec: Decoder[Credentials] = deriveDecoder[Credentials]
  implicit val enc: Encoder[Credentials] = deriveEncoder[Credentials]

  implicit def decodeMap[F[_]: Sync]: MapConvert[F, ValidationResult[Credentials]] =
    (values: Map[String, String]) =>
      (
        values
          .get("email")
          .map(EmailAddress.unsafeFrom(_).validNec)
          .getOrElse("Field [ email ] isn't defined".invalidNec),
        values
          .get("password")
          .map(Password.unsafeFrom(_).validNec)
          .getOrElse("Field [ password ] isn't defined".invalidNec)
      ).mapN(Credentials.apply).pure[F]
}
