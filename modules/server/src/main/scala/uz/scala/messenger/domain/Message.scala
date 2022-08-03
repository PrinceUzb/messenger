package uz.scala.messenger.domain

import derevo.cats.show
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import uz.scala.messenger.types.{ Content, MessageId, UserId }

import java.time.ZonedDateTime

@derive(decoder, encoder, show)
case class Message(
    id: MessageId,
    content: Content,
    createdAt: ZonedDateTime,
    senderId: UserId,
    receiverId: UserId,
    seen: Boolean = false,
  )

object Message {
  @derive(decoder, encoder, show)
  case class SendMessage(content: Content, receiverId: UserId)
}
