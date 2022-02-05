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
}

object MessageAlgebra {
  def apply[F[_]](implicit F: Sync[F], session: Resource[F, Session[F]]): F[MessageAlgebra[F]] =
    F.delay(
      new LiveMessageAlgebra[F]
    )

  final class LiveMessageAlgebra[F[_]](implicit F: Sync[F], session: Resource[F, Session[F]])
      extends SkunkHelper[F]
      with MessageAlgebra[F] {

    override def create(from: UUID, sendMessage: SendMessage): F[Message] =
      GenUUID[F].make.flatMap { uuid =>
        session.use(_.prepare(insert).use(_.unique(uuid ~ from ~ sendMessage)))
      }

  }

}
