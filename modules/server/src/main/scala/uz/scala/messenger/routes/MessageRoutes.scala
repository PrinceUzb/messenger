package uz.scala.messenger.routes

import cats.effect._
import cats.implicits.toFlatMapOps
import fs2.concurrent.Topic
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame.Text
import org.typelevel.log4cats.Logger
import tsec.authentication.asAuthed
import uz.scala.messenger.domain._
import uz.scala.messenger.domain.custom.exception.DeliveryFailure
import uz.scala.messenger.implicits.{CirceDecoderOps, GenericTypeOps}
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.services.{MessageSender, Messages}

object MessageRoutes {
  val prefixPath = "/message"
  def apply[F[_]: Async: Sync: Logger](sender: MessageSender[F], messages: Messages[F])(implicit
    authService: AuthService[F, User],
    topic: Topic[F, Message]
  ): MessageRoutes[F] =
    new MessageRoutes(sender, messages)
}

final class MessageRoutes[F[_]: Async](sender: MessageSender[F], messages: Messages[F])(implicit
  logger: Logger[F],
  authService: AuthService[F, User],
  topic: Topic[F, Message]
) {

  implicit object dsl extends Http4sDsl[F]
  import dsl._

  val routes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb =>
    authService.securedRoutes {
      case GET -> Root asAuthed user =>
      wsb.build(
        topic.subscribe(1000).filter(_.to == user.id).map { msg =>
          Text(msg.toJson)
        },
        _.flatMap {
          case Text(data, _) =>
            sender.send(user.id, data.as[SendMessage])
          case _ =>
            fs2.Stream.raiseError(DeliveryFailure.ParseFailure)
        }
      )
      case GET -> Root / UUIDVar(userId) asAuthed user =>
        messages.getAll(user.id, userId).flatMap(Ok(_))
    }
}
