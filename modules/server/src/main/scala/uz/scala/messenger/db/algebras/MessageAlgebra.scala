package uz.scala.messenger.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import eu.timepit.refined.types.string.NonEmptyString
import skunk.Session
import skunk.implicits.toIdOps
import uz.scala.messenger.db.sql.MessageSql._
import uz.scala.messenger.domain.Message
import uz.scala.messenger.utils.GenUUID

import java.util.UUID

trait MessageAlgebra[F[_]] {
  def create(to: UUID, from: UUID, text: NonEmptyString): F[Message]
}

object LiveMessageAlgebra {
  def apply[F[_]](
    sessionPool: Resource[F, Session[F]]
  )(implicit F: Sync[F]): F[UserAlgebra[F]] =
    F.delay(
      new LiveMessageAlgebra[F](sessionPool)
    )
}

final class LiveMessageAlgebra[F[_]] private (
  sessionPool: Resource[F, Session[F]]
)(implicit F: Sync[F])
    extends SkunkHelper[F]
    with MessageAlgebra[F] {

  private implicit val sp: Resource[F, Session[F]] = sessionPool

  override def create(to: UUID, from: UUID, text: NonEmptyString): F[Message] =
    GenUUID[F].make.flatMap { uuid =>
      sessionPool.use(_.prepare(insert).use(_.unique(uuid ~ to ~ from ~ text)))
    }

}
