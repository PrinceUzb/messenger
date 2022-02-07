package uz.scala.messenger.modules

import cats.effect._
import cats.effect.std.Console
import eu.timepit.refined.auto.autoUnwrap
import natchez.Trace.Implicits.noop
import skunk._
import skunk.util.Typer
import uz.scala.messenger.config.DBConfig
import uz.scala.messenger.db.algebras.{MessageAlgebra, UserAlgebra}

trait Database[F[_]] {
  val user: F[UserAlgebra[F]]
  val message: F[MessageAlgebra[F]]
}

object Database {
  def apply[F[_]: Async: Console](config: DBConfig)(implicit F: Sync[F]): F[Database[F]] =
    Session
      .pooled[F](
        host = config.host,
        port = config.port,
        database = config.database,
        user = config.user,
        password = Some(config.password),
        max = config.poolSize,
        strategy = Typer.Strategy.SearchPath
      )
      .use { implicit session =>
        F.delay(new LiveDatabase[F])
      }

  final class LiveDatabase[F[_]: Async: Console](implicit
    session: Resource[F, Session[F]]
  ) extends Database[F] {

    override val user: F[UserAlgebra[F]]       = UserAlgebra[F]
    override val message: F[MessageAlgebra[F]] = MessageAlgebra[F]
  }
}
