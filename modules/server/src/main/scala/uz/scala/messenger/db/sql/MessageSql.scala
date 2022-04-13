package uz.scala.messenger.db.sql

import eu.timepit.refined.types.string.NonEmptyString
import skunk._
import skunk.codec.all.{bool, timestamp, uuid, varchar}
import skunk.implicits.{toIdOps, toStringOps}
import uz.scala.messenger.domain.{Message, SendMessage}

import java.time.LocalDateTime
import java.util.UUID

object MessageSql {

  val messageTableColumns = uuid ~ uuid ~ uuid ~ varchar ~ timestamp ~ bool
  val dec: Decoder[Message] = messageTableColumns.map { case id ~ to ~ from ~ text ~ createdAt ~ _ =>
    Message(id, to, from, NonEmptyString.unsafeFrom(text), createdAt)
  }

  val enc: Encoder[UUID ~ UUID ~ SendMessage] = messageTableColumns.contramap { case id ~ from ~ sendMessage =>
    id ~ sendMessage.to ~ from ~ sendMessage.text.value ~ LocalDateTime.now() ~ false
  }

  val insert: Query[UUID ~ UUID ~ SendMessage, Message] =
    sql"""INSERT INTO messages VALUES ($enc) RETURNING *""".query(dec)

  val selectAll: Query[UUID ~ UUID, Message] =
    sql"""SELECT * FROM messages WHERE ("to" = $uuid AND "from" = $uuid) OR ("to" = $uuid AND "from" = $uuid) ORDER BY created_at DESC""".query(dec)
      .contramap[UUID ~ UUID] { case ownerId ~ userId =>
        ownerId ~ userId ~ userId ~ ownerId
      }

}
