package uz.scala.messenger.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.util.UUID

case class Message(
  id: UUID,
  to: UUID,
  from: UUID,
  text: NonEmptyString
)

object Message {
  implicit val dec: Decoder[Message] = deriveDecoder
  implicit val enc: Encoder[Message] = deriveEncoder
}
