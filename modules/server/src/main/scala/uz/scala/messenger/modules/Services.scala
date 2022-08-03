package uz.scala.messenger.modules

import cats.effect.{ Resource, Sync }
import org.typelevel.log4cats.Logger
import skunk.Session
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.services.{ Messages, Users }
import uz.scala.messenger.services.redis.RedisClient

object Services {
  def apply[F[_]: GenUUID: Logger: Sync](
      redisClient: RedisClient[F]
    )(implicit
      session: Resource[F, Session[F]]
    ): Services[F] =
    new Services[F](
      users = Users[F],
      messages = Messages.make[F],
    )
}

final class Services[F[_]] private (
    val users: Users[F],
    val messages: Messages[F],
  )
