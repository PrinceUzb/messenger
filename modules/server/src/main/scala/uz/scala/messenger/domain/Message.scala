package uz.scala.messenger.domain

import uz.scala.messenger.types.{ Content, MessageId, UserId }

import java.time.ZonedDateTime

case class Message(
    id: MessageId,
    content: Content,
    createdAt: ZonedDateTime,
    senderId: UserId,
    receiverId: UserId,
    seen: Boolean = false,
  )

object Message {
  case class SendMessage(content: Content, receiverId: UserId)
}
