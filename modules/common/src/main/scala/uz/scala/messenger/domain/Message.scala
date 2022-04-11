package uz.scala.messenger.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDateTime
import java.util.UUID

case class Message(
  id: UUID,
  to: UUID,
  from: UUID,
  text: NonEmptyString,
  created_at: LocalDateTime
)

object Message {
  implicit val dec: Decoder[Message] = deriveDecoder
  implicit val enc: Encoder[Message] = deriveEncoder
}
