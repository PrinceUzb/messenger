package uz.scala.messenger.routes

import cats.effect._
import cats.effect.std.{Queue, QueueSink}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame.Text
import org.typelevel.log4cats.Logger
import uz.scala.messenger.domain._
import uz.scala.messenger.domain.custom.exception.DeliveryFailure
import uz.scala.messenger.implicits.{CirceDecoderOps, GenericTypeOps}
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.services.MessageSender

import java.nio.charset.StandardCharsets.UTF_8

object MessageRoutes {
  val prefixPath = "/message"
  def apply[F[_]: Async: Sync: Logger](sender: MessageSender[F])(implicit
    authService: AuthService[F, User],
    queue: Queue[F, Message]
  ): MessageRoutes[F] =
    new MessageRoutes(sender)
}

final class MessageRoutes[F[_]: Async](sender: MessageSender[F])(implicit
  logger: Logger[F],
  authService: AuthService[F, User],
  queue: Queue[F, Message]
) {

  implicit object dsl extends Http4sDsl[F]
  import dsl._

  val routes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb =>
    HttpRoutes.of { case GET -> Root / UUIDVar(from) =>
      wsb.build(
        fs2.Stream.fromQueueUnterminated(queue).filter(_.from != from).map { msg =>
          println(msg)
          Text(msg.toJson)
        },
        _.flatMap {
          case Text(data, _) =>
            sender.send(from, data.as[SendMessage])
          case _ =>
            fs2.Stream.raiseError(DeliveryFailure.ParseFailure)
        }
      )
    }
}
