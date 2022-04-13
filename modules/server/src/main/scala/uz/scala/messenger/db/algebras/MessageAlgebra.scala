package uz.scala.messenger.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import skunk.Session
import skunk.implicits.toIdOps
import uz.scala.messenger.db.sql.MessageSql._
import uz.scala.messenger.domain.{Message, SendMessage}
import uz.scala.messenger.utils.GenUUID

import java.util.UUID

trait MessageAlgebra[F[_]] {
  def create(from: UUID, sendMessage: SendMessage): F[Message]
  def getAll(ownerId: UUID, userId: UUID): F[List[Message]]
}

object MessageAlgebra {
  def apply[F[_]: Sync](implicit session: Resource[F, Session[F]]): MessageAlgebra[F] = new MessageAlgebra[F]
    with SkunkHelper[F] {

    override def create(from: UUID, sendMessage: SendMessage): F[Message] =
      GenUUID[F].make.flatMap { uuid =>
        session.use(_.prepare(insert).use(_.unique(uuid ~ from ~ sendMessage)))
      }

    override def getAll(ownerId: UUID, userId: UUID): F[List[Message]] =
      prepListQuery(selectAll, ownerId ~ userId)

  }

}
