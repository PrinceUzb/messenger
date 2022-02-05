package uz.scala.messenger.routes

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import fs2.Pipe
import fs2.concurrent.Topic
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import org.typelevel.log4cats.Logger
import tsec.authentication._
import tsec.authentication.credentials.CredentialsError
import uz.scala.messenger.domain._
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.services.UserService

import scala.concurrent.duration.DurationInt

object MessageRoutes {
  val prefixPath = "/message"
  def apply[F[_]: Async: Sync](implicit logger: Logger[F], authService: AuthService[F, User]): MessageRoutes[F] =
    new MessageRoutes
}

final class MessageRoutes[F[_]: Async](messageTopic: Topic[F, Message])(implicit
  logger: Logger[F],
  authService: AuthService[F, User],
  F: Sync[F]
) {

  implicit object dsl extends Http4sDsl[F]
  import dsl._

  val routes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb =>
    authService.securedRoutes {
      case GET -> Root asAuthed user =>
        val publisher = fs2.Stream.constant("1").covary[F].through(messageTopic.publish)
        val subscriber = messageTopic.subscribe(10).take(4)
    }
}
