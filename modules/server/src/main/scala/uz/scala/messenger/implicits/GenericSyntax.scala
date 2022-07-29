package uz.scala.messenger.implicits

import io.circe.syntax.EncoderOps
import io.circe.{ Encoder, Printer }

trait GenericSyntax {
  implicit class GenericTypeOps[A](obj: A) {
    private val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

    def toOptWhen(cond: => Boolean): Option[A] = if (cond) Some(obj) else None

    def toJson(implicit encoder: Encoder[A]): String = obj.asJson.printWith(printer)
  }
}
