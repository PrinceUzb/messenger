package uz.scala.messenger.services.sql

import skunk._
import skunk.implicits._
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.scala.messenger.domain.User
import uz.scala.messenger.domain.User.UserStatus.Offline
import uz.scala.messenger.domain.User.{ CreateUser, UserWithPassword }
import uz.scala.messenger.domain.custom.refinements.Tel
import uz.scala.messenger.types._

object UserSQL extends SQLTable {
  val userId: Codec[UserId] = identifier[UserId]

  private val columns = userId ~ username ~ tel ~ status ~ passwordHash

  val encoder: Encoder[UserId ~ CreateUser ~ PasswordHash[SCrypt]] =
    columns.contramap {
      case i ~ u ~ p =>
        i ~ u.username ~ u.phone ~ Offline ~ p
    }

  val decoder: Decoder[User] =
    columns.map {
      case i ~ n ~ t ~ s ~ _ =>
        User(i, n, t, s)
    }

  val common: Decoder[UserWithPassword] =
    columns.map {
      case i ~ n ~ t ~ s ~ p =>
        UserWithPassword(User(i, n, t, s), p)
    }

  val selectByPhone: Query[Tel, UserWithPassword] =
    sql"""SELECT * FROM users WHERE phone = $tel""".query(common)

  val insert: Query[UserId ~ CreateUser ~ PasswordHash[SCrypt], User] =
    sql"""INSERT INTO users VALUES ($encoder) returning *""".query(decoder)
}
