package uz.scala.messenger.modules

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import fs2.concurrent.Topic
import org.typelevel.log4cats.Logger
import uz.scala.messenger.db.algebras.Algebras
import uz.scala.messenger.domain.Message
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.{LiveUserService, MessageSender, UserService}

object MessengerProgram {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F],
    redisClient: RedisClient[F],
    queue: Queue[F, Message]
  ): F[MessengerProgram[F]] = {
    implicit val redis: RedisClient[F] = redisClient

    def algebrasF: F[Algebras[F]] = (
      database.user,
      database.message
    ).mapN(Algebras.apply)

    for {
      algebras      <- algebrasF
      auth          <- Authentication[F](algebras.user)
      messageSender <- MessageSender[F](algebras.message, queue)
      userService   <- LiveUserService[F](algebras.user)
    } yield new MessengerProgram[F](auth, userService, messageSender)
  }
}

final class MessengerProgram[F[_]: Sync] private (
  val auth: Authentication[F],
  val userService: UserService[F],
  val messageSender: MessageSender[F]
)
