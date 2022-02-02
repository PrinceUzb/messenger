package uz.scala.messenger.modules

import cats.effect._
import cats.implicits._
import uz.scala.messenger.db.algebras.Algebras
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.{LiveUserService, UserService}
import org.typelevel.log4cats.Logger

object MessengerProgram {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F],
    redisClient: RedisClient[F]
  ): F[MessengerProgram[F]] = {
    implicit val redis: RedisClient[F] = redisClient

    def algebrasF: F[Algebras[F]] = (
      database.user
    ).map(Algebras.apply)

    for {
      algebras <- algebrasF
      auth <- Authentication[F](algebras.user)
      userService <- LiveUserService[F](algebras.user)
    } yield new MessengerProgram[F](auth, userService)
  }
}

final class MessengerProgram[F[_]: Sync] private (
  val auth: Authentication[F],
  val userService: UserService[F]
)
