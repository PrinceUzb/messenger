package uz.scala.messenger.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class EmailAndPassword (email: String, password: String)
object EmailAndPassword {
  implicit val dec: Decoder[EmailAndPassword] = deriveDecoder
  implicit val enc: Encoder[EmailAndPassword] = deriveEncoder
}
