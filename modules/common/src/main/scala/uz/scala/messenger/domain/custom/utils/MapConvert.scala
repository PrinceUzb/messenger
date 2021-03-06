package uz.scala.messenger.domain.custom.utils

import cats.data.ValidatedNec

object MapConvert {
  type ValidationResult[A] = ValidatedNec[String, A]
}

trait MapConvert[F[_], A] {
  def fromMap(values: Map[String, String]): F[A]
}