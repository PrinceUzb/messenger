package uz.scala.messenger.services

import cats.effect.{ Resource, Sync }
import cats.implicits._
import skunk.Session
import uz.scala.messenger.domain.Message.SendMessage
import uz.scala.messenger.domain.{ ID, Message }
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.services.sql.MessageSQL
import uz.scala.messenger.types.{ MessageId, UserId }

import java.time.ZonedDateTime

trait Messages[F[_]] {
  def create(send: SendMessage, sender: UserId): F[Message]
}

object Messages {
  def make[F[_]: GenUUID: Sync](implicit session: Resource[F, Session[F]]): Messages[F] =
    new Messages[F] with SkunkHelper[F] {
      override def create(send: SendMessage, sender: UserId): F[Message] =
        for {
          id <- ID.make[F, MessageId]
          now <- Sync[F].delay(ZonedDateTime.now())

          message <- prepQueryUnique(
            MessageSQL.insert,
            Message(id, send.content, now, sender, send.receiverId),
          )
        } yield message
    }
}
