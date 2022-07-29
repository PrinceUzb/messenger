package uz.scala.messenger.domain

import cats.Functor
import cats.implicits.toFunctorOps
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.types.IsUUID

object ID {
  def make[F[_]: Functor: GenUUID, A: IsUUID]: F[A] =
    GenUUID[F].make.map(IsUUID[A]._UUID.get)

  def read[F[_]: Functor: GenUUID, A: IsUUID](str: String): F[A] =
    GenUUID[F].read(str).map(IsUUID[A]._UUID.get)
}
