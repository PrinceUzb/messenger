package uz.scala.messenger.services

import cats.effect.Sync
import cats.effect.std.Queue
import fs2.concurrent.Topic
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame.Text
import uz.scala.messenger.domain.Message
import uz.scala.messenger.implicits.CirceEncoderOps

trait MessageSender[F[_]] {
  def send(message: Message): F[Unit]
}

object MessageSender {
  def apply[F[_]](implicit ev: MessageSender[F]): MessageSender[F] = ev
  implicit def syncMessageSender[F[_]: Sync]: MessageSender[F] = ???

  class MessageSenderImpl[F[_]](topic: Topic[F, Message], queue: Queue[F, Message]) {
    override def send(message: Message): F[Unit] =
      topic.subscribe(1000).map(message => Text(message.toJson))
    WebSocketBuilder2[F].build()

  }
}
