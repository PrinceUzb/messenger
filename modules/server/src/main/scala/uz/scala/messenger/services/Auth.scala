package uz.scala.messenger.services

import cats.effect.Sync
import cats.syntax.all._
import uz.scala.messenger.domain.User._
import uz.scala.messenger.domain._
import uz.scala.messenger.domain.custom.exception.{ InvalidPassword, PhoneInUse, UserNotFound }
import uz.scala.messenger.domain.custom.refinements.Tel
import uz.scala.messenger.security.Tokens
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.types.TokenExpiration
import dev.profunktor.auth.jwt.JwtToken
import eu.timepit.refined.auto.autoUnwrap
import tsec.passwordhashers.jca.SCrypt
import uz.scala.messenger.domain.Credentials

trait Auth[F[_]] {
  def newUser(createUser: CreateUser): F[JwtToken]
  def login(credentials: Credentials): F[JwtToken]
  def logout(token: JwtToken, phone: Tel): F[Unit]
}

object Auth {
  def apply[F[_]: Sync](
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      users: Users[F],
      redis: RedisClient[F],
    ): Auth[F] =
    new Auth[F] {
      private val TokenExpiration = tokenExpiration.value

      override def newUser(createUser: CreateUser): F[JwtToken] =
        users.find(createUser.phone).flatMap {
          case Some(_) =>
            PhoneInUse(createUser.phone).raiseError[F, JwtToken]
          case None =>
            for {
              hash <- SCrypt.hashpw[F](createUser.password)
              user <- users.create(createUser, hash)
              t <- tokens.create
              _ <- redis.put(t.value, user, TokenExpiration)
              _ <- redis.put(user.phone, t.value, TokenExpiration)
            } yield t
        }

      def login(credentials: Credentials): F[JwtToken] =
        users.find(credentials.phone).flatMap {
          case None =>
            UserNotFound(credentials.phone).raiseError[F, JwtToken]
          case Some(user) if !SCrypt.checkpwUnsafe(credentials.password, user.password) =>
            InvalidPassword(credentials.phone).raiseError[F, JwtToken]
          case Some(userWithPass) =>
            redis.get(credentials.phone).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.put(t.value, userWithPass.user, TokenExpiration) *>
                    redis.put(credentials.phone, t.value, TokenExpiration)
                }
            }
        }

      def logout(token: JwtToken, phone: Tel): F[Unit] =
        redis.del(token.show, phone)
    }
}
