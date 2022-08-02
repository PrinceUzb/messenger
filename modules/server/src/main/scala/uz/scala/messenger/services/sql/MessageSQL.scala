package uz.scala.messenger.services.sql

import skunk._
import skunk.codec.all.bool
import skunk.implicits._
import uz.scala.messenger.domain.Message
import uz.scala.messenger.types._

object MessageSQL extends SQLTable {
  val messageId: Codec[MessageId] = identifier[MessageId]

  private val columns =
    messageId ~ content ~ zonedDateTime ~ UserSQL.userId ~ UserSQL.userId ~ bool ~ bool

  val codec: Codec[Message] =
    columns.imap {
      case i ~ c ~ t ~ sid ~ rid ~ seen ~ _ =>
        Message(i, c, t, sid, rid, seen)
    }(m => m.id ~ m.content ~ m.createdAt ~ m.senderId ~ m.receiverId ~ m.seen ~ false)

  val insert: Query[Message, Message] =
    sql"""INSERT INTO messages VALUES ($codec) returning *""".query(codec)
}
