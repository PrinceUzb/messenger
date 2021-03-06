package uz.scala.messenger.services

import cats.effect.Sync
import cats.effect.std.Queue
import uz.scala.messenger.db.algebras.MessageAlgebra
import uz.scala.messenger.domain.{Message, SendMessage}

import java.util.UUID

trait MessageSender[F[_]] {
  def send(form: UUID, sendMessage: SendMessage): fs2.Stream[F, Unit]
}

object MessageSender {
  def apply[F[_]: Sync](messageAlgebra: MessageAlgebra[F], queue: Queue[F, Message]): F[MessageSender[F]] =
    Sync[F].delay(new MessageSenderImpl[F](messageAlgebra, queue))

  private[this] class MessageSenderImpl[F[_]: Sync](messageAlgebra: MessageAlgebra[F], queue: Queue[F, Message])
      extends MessageSender[F] {
    override def send(form: UUID, sendMessage: SendMessage): fs2.Stream[F, Unit] =
      fs2.Stream.eval(messageAlgebra.create(form, sendMessage)).enqueueUnterminated(queue)

  }
}
