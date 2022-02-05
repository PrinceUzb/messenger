package uz.scala.messenger.db.sql

import uz.scala.messenger.domain.custom.refinements.{Nickname, _}
import uz.scala.messenger.domain.{User, UserData}
import uz.scala.messenger.implicits.PasswordOps
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.time.LocalDateTime
import java.util.UUID

object UserSql {
  val emailCodec: Codec[EmailAddress] = varchar.imap(email => EmailAddress.unsafeFrom(email))(email => email.value)
  val nicknameCodec: Codec[Nickname] = varchar.imap(email => Nickname.unsafeFrom(email))(email => email.value)
  val dec: Decoder[User] = (uuid ~ emailCodec ~ nicknameCodec ~ timestamp ~ varchar).map {
    case id ~ email ~ nickname ~ createdAt ~ _ =>
      User(
        id = id,
        nickname = nickname,
        createdAt = createdAt,
        email = email
      )
  }

  val enc: Encoder[UUID ~ UserData] = (uuid ~ emailCodec ~ nicknameCodec ~ timestamp ~ varchar).contramap { case id ~ u =>
    id ~ u.email ~ u.nickname ~ LocalDateTime.now() ~ u.password.toHashUnsafe
  }

  val insert: Query[UUID ~ UserData, User] =
    sql"""INSERT INTO users VALUES ($enc) RETURNING *""".query(dec)

  val selectByEmail: Query[EmailAddress, User] =
    sql"""SELECT * FROM users WHERE email = $emailCodec """.query(dec)

  val selectPass: Query[EmailAddress, String] =
    sql"""SELECT password_hash FROM users WHERE email = $emailCodec """.query(varchar)

}
