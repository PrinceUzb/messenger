package uz.scala.messenger.modules

import cats.effect.{Async, Sync}
import cats.implicits._
import uz.scala.messenger.db.algebras.{IdentityProvider, UserAlgebra}
import uz.scala.messenger.domain.User
import uz.scala.messenger.security.{AuthService, LiveAuthService}
import uz.scala.messenger.services.LiveIdentityService
import uz.scala.messenger.services.redis.RedisClient

object Authentication {
  private[this] def makeAuthService[F[_]: Async: Sync, U](
    identityProvider: IdentityProvider[F, U]
  )(implicit redisClient: RedisClient[F]): F[AuthService[F, U]] =
    LiveIdentityService[F, U](identityProvider).flatMap(LiveAuthService[F, U])

  def apply[F[_]: Async](
    userProvider: UserAlgebra[F]
  )(implicit F: Sync[F], redisClient: RedisClient[F]): F[Authentication[F]] =
    for {
      userAuth <- makeAuthService[F, User](userProvider)
    } yield new Authentication[F](userAuth)
}

final class Authentication[F[_]] private (
  val user: AuthService[F, User]
)
