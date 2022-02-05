package uz.scala.messenger.services

import cats.effect.Sync
import cats.effect.kernel.Async
import cats.effect.std.Queue
import uz.scala.messenger.domain.Message

trait MessageSender[F[_]] {
  def send(message: Message): fs2.Stream[F, Message]
}

object MessageSender {
  def apply[F[_]: Sync](queue: Queue[F, Message]): MessageSender[F] =
    new MessageSenderImpl[F](queue)

  private[this] class MessageSenderImpl[F[_]: Sync](queue: Queue[F, Message]) extends MessageSender[F] {
    override def send(message: Message): fs2.Stream[F, Message] =
      fs2.Stream.fromQueueUnterminated(queue)

  }
}
