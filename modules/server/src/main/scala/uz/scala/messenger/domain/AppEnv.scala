package uz.scala.messenger.domain

import cats.Show
import io.circe.{ Decoder, Encoder }

sealed trait AppEnv {
  def value: String = this.toString
}

object AppEnv {
  case object TEST extends AppEnv

  case object DEV extends AppEnv

  case object PROD extends AppEnv

  val all: List[AppEnv] = List(TEST, DEV, PROD)

  def find(value: String): Option[AppEnv] =
    all.find(_.value == value.toUpperCase)

  def unsafeFrom(value: String): AppEnv =
    find(value).getOrElse(throw new IllegalArgumentException(s"value doesn't match [ $value ]"))

  implicit val enc: Encoder[AppEnv] = Encoder.encodeString.contramap[AppEnv](_.value)
  implicit val dec: Decoder[AppEnv] = Decoder.decodeString.map(unsafeFrom)
  implicit val show: Show[AppEnv] = Show.show(_.value)
}
