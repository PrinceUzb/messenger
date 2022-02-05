package uz.scala.messenger


import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Printer}

package object implicits {
  implicit class CirceDecoderOps(s: String) {
    def as[A: Decoder]: A = decode[A](s).fold(throw _, json => json)
  }

  implicit class GenericTypeOps[A](obj: A) {
    private val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

    def toOptWhen(cond: => Boolean): Option[A] = if (cond) Some(obj) else None

    def toJson(implicit encoder: Encoder[A]): String = obj.asJson.printWith(printer)
  }

}
