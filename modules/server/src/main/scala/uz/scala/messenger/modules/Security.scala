package uz.scala.messenger.modules

import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import pdi.jwt._
import uz.scala.messenger.config.AppConfig
import uz.scala.messenger.security.{ JwtExpire, Tokens }
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.{ Auth, Users }
import uz.scala.messenger.types.UserJwtAuth

object Security {
  def apply[F[_]: Sync](
      cfg: AppConfig,
      users: Users[F],
      redis: RedisClient[F],
    ): F[Security[F]] =
    for {
      tokens <- JwtExpire[F].map(
        Tokens.make[F](_, cfg.jwtConfig.tokenConfig.value, cfg.jwtConfig.tokenExpiration)
      )
      userJwtAuth = UserJwtAuth(
        JwtAuth.hmac(cfg.jwtConfig.tokenConfig.value.secret, JwtAlgorithm.HS256)
      )
      auth = Auth[F](cfg.jwtConfig.tokenExpiration, tokens, users, redis)
    } yield new Security[F](auth, userJwtAuth)
}

final class Security[F[_]] private (
    val auth: Auth[F],
    val userJwtAuth: UserJwtAuth,
  )
