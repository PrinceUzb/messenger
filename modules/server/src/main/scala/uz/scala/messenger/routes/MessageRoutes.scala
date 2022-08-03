package uz.scala.messenger.routes

import cats.MonadThrow
import cats.effect.Async
import cats.effect.std.Queue
import fs2.concurrent.Topic
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame.Text
import org.typelevel.log4cats.Logger
import uz.scala.messenger.domain.Message.SendMessage
import uz.scala.messenger.domain.custom.exception.DeliveryFailure
import uz.scala.messenger.domain.{ Message, User }
import uz.scala.messenger.implicits.{ CirceDecoderOps, GenericTypeOps }
import uz.scala.messenger.services.Messages

object MessageRoutes {
  def apply[F[_]: Async: Logger](
      messages: Messages[F]
    )(implicit
      topic: Topic[F, Message],
      queue: Queue[F, Message],
    ): MessageRoutes[F] =
    new MessageRoutes(messages)
}

final class MessageRoutes[F[_]: JsonDecoder: MonadThrow](
    messages: Messages[F]
  )(implicit
    topic: Topic[F, Message],
    queue: Queue[F, Message],
  ) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/message"

  private[this] val httpRoutes: WebSocketBuilder2[F] => AuthedRoutes[User, F] = wsb =>
    AuthedRoutes.of {
      case GET -> Root as user =>
        wsb.build(
          topic.subscribe(1000).filter(_.receiverId == user.id).map { msg =>
            Text(msg.toJson)
          },
          _.flatMap {
            case Text(data, _) =>
              fs2
                .Stream
                .eval(messages.create(data.as[SendMessage], user.id))
                .enqueueUnterminated(queue)

            case _ =>
              fs2.Stream.raiseError(DeliveryFailure.ParseFailure)
          },
        )
    }

  def routes(authMiddleware: AuthMiddleware[F, User]): WebSocketBuilder2[F] => HttpRoutes[F] =
    wsb =>
      Router(
        prefixPath -> authMiddleware(httpRoutes(wsb))
      )
}
