package uz.scala.messenger.services

import uz.scala.messenger.db.algebras.MessageAlgebra
import uz.scala.messenger.domain.Message

import java.util.UUID

trait Messages[F[_]] {
  def getAll(ownerId: UUID, userId: UUID): F[List[Message]]
}

object Messages {
  def apply[F[_]](messageAlgebra: MessageAlgebra[F]): Messages[F] = new Messages[F] {
      override def getAll(ownerId: UUID, userId: UUID): F[List[Message]] =
        messageAlgebra.getAll(ownerId, userId)
    }
}
