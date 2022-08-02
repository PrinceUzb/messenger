package uz.scala.messenger.services

import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.scala.messenger.domain.User.{ CreateUser, UserWithPassword }
import uz.scala.messenger.domain.custom.exception.PhoneInUse
import uz.scala.messenger.domain.custom.refinements.Tel
import uz.scala.messenger.domain.{ ID, User }
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.sql.UserSQL._
import uz.scala.messenger.types.UserId

import java.time.ZonedDateTime

trait Users[F[_]] {
  def find(phone: Tel): F[Option[UserWithPassword]]
  def create(createUser: CreateUser, password: PasswordHash[SCrypt]): F[User]
}

object Users {
  def apply[F[_]: GenUUID: Sync](
      redis: RedisClient[F]
    )(implicit
      session: Resource[F, Session[F]]
    ): Users[F] =
    new Users[F] with SkunkHelper[F] {
      def find(phone: Tel): F[Option[UserWithPassword]] =
        OptionT(prepOptQuery(selectByPhone, phone)).value

      def create(createUser: CreateUser, password: PasswordHash[SCrypt]): F[User] =
        for {
          id <- ID.make[F, UserId]
          now <- Sync[F].delay(ZonedDateTime.now())
          user <- OptionT(find(createUser.phone))
            .semiflatMap(_ => PhoneInUse(createUser.phone).raiseError[F, User])
            .getOrElseF(prepQueryUnique(insert, id ~ now ~ createUser ~ password))
        } yield user
    }
}
