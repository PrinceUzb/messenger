package uz.scala.messenger.modules

import cats.effect.Resource
import cats.effect.kernel.Async
import org.typelevel.log4cats.Logger
import skunk.Session
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.services.Users
import uz.scala.messenger.services.redis.RedisClient

object Services {
  def apply[F[_]: Async: GenUUID: Logger](
      redisClient: RedisClient[F]
    )(implicit
      session: Resource[F, Session[F]]
    ): Services[F] =
    new Services[F](
      users = Users[F](redisClient)
    )
}

final class Services[F[_]] private (
    val users: Users[F]
  )
