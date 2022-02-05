package uz.scala.messenger.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID
import io.circe.refined._

case class SendMessage(
  to: UUID,
  text: NonEmptyString
)

object SendMessage {
  implicit val dec: Decoder[SendMessage] = deriveDecoder
  implicit val enc: Encoder[SendMessage] = deriveEncoder
}
