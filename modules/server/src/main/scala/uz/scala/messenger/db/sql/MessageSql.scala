package uz.scala.messenger.db.sql

import eu.timepit.refined.auto.autoRefineV
import eu.timepit.refined.types.string.NonEmptyString
import skunk._
import skunk.codec.all.{bool, uuid, varchar}
import skunk.implicits.{toIdOps, toStringOps}
import uz.scala.messenger.domain.{Message, User, UserData}

import java.util.UUID

object MessageSql {

  val messageTableColumns: Codec[((((UUID, UUID), UUID), String), Boolean)] = uuid ~ uuid ~ uuid ~ varchar ~ bool
  val dec: Decoder[Message] = messageTableColumns.map { case id ~ to ~ from ~ text ~ _ =>
    Message(id, to, from, autoRefineV(text))
  }

  val enc: Encoder[UUID ~ UUID ~ UUID ~ NonEmptyString] = messageTableColumns.contramap { case id ~ to ~ from ~ text =>
    id ~ to ~ from ~ text.value ~ false
  }

  val insert: Query[UUID ~ UUID ~ UUID ~ NonEmptyString, Message] =
    sql"""INSERT INTO users VALUES ($enc) RETURNING *""".query(dec)

}
