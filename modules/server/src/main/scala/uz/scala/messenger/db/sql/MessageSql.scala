package uz.scala.messenger.db.sql

import eu.timepit.refined.types.string.NonEmptyString
import skunk._
import skunk.codec.all.{bool, uuid, varchar}
import skunk.implicits.{toIdOps, toStringOps}
import uz.scala.messenger.domain.{Message, SendMessage}

import java.util.UUID

object MessageSql {

  val messageTableColumns: Codec[((((UUID, UUID), UUID), String), Boolean)] = uuid ~ uuid ~ uuid ~ varchar ~ bool
  val dec: Decoder[Message] = messageTableColumns.map { case id ~ to ~ from ~ text ~ _ =>
    Message(id, to, from, NonEmptyString.unsafeFrom(text))
  }

  val enc: Encoder[UUID ~ UUID ~ SendMessage] = messageTableColumns.contramap { case id ~ from ~ sendMessage =>
    id ~ sendMessage.to ~ from ~ sendMessage.text.value ~ false
  }

  val insert: Query[UUID ~ UUID ~ SendMessage, Message] =
    sql"""INSERT INTO messages VALUES ($enc) RETURNING *""".query(dec)

}
