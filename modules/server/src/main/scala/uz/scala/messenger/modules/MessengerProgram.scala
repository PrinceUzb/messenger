package uz.scala.messenger.modules

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import org.typelevel.log4cats.Logger
import uz.scala.messenger.domain.Message
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.{LiveUserService, MessageSender, Messages, UserService}

object MessengerProgram {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F],
    redisClient: RedisClient[F],
    queue: Queue[F, Message]
  ): F[MessengerProgram[F]] = {
    implicit val redis: RedisClient[F] = redisClient

    for {
      auth <- Authentication[F](database.user)
      messageSender = MessageSender[F](database.message, queue)
      messages      = Messages[F](database.message)
      userService <- LiveUserService[F](database.user)
    } yield new MessengerProgram[F](auth, userService, messageSender, messages)
  }
}

final class MessengerProgram[F[_]: Sync] private (
  val auth: Authentication[F],
  val userService: UserService[F],
  val messageSender: MessageSender[F],
  val messages: Messages[F]
)
