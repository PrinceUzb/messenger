package uz.scala.messenger.modules

import cats.effect._
import cats.effect.std.Queue
import fs2.concurrent.Topic
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.{Router, middleware}
import org.http4s.{HttpApp, HttpRoutes}
import org.typelevel.log4cats.Logger
import uz.scala.messenger.config.LogConfig
import uz.scala.messenger.domain.{Message, User}
import uz.scala.messenger.routes._
import uz.scala.messenger.security.AuthService

object HttpApi {
  def apply[F[_]: Async: Logger](
    program: MessengerProgram[F],
    topic: Topic[F, Message],
    logConfig: LogConfig,
    queue: Queue[F, Message]
  )(implicit F: Sync[F]): F[HttpApi[F]] =
    F.delay(
      new HttpApi[F](program, topic, logConfig, queue)
    )
}

final class HttpApi[F[_]: Async: Logger] private (
  program: MessengerProgram[F],
  topic: Topic[F, Message],
  logConfig: LogConfig,
  queue: Queue[F, Message]
) {
  private[this] val root: String        = "/"
  private[this] val webjarsPath: String = "/webjars"

  implicit val authUser: AuthService[F, User] = program.auth.user
  implicit val mt: Topic[F, Message]          = topic
  implicit val mq: Queue[F, Message]          = queue

  private[this] val rootRoutes: HttpRoutes[F] = RootRoutes[F].routes
  private[this] val userRoutes: HttpRoutes[F] = UserRoutes[F](program.userService).routes
  private[this] val messageRoutes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb =>
    MessageRoutes[F](program.messageSender, program.messages).routes(wsb)
  private[this] val webjars: HttpRoutes[F] = webjarServiceBuilder[F].toRoutes

  private[this] val loggedRoutes: HttpRoutes[F] => HttpRoutes[F] = http =>
    middleware.Logger.httpRoutes(logConfig.httpHeader, logConfig.httpBody)(http)

  val httpApp: WebSocketBuilder2[F] => HttpApp[F] = wsb =>
    loggedRoutes(
      Router(
        webjarsPath              -> webjars,
        UserRoutes.prefixPath    -> userRoutes,
        MessageRoutes.prefixPath -> messageRoutes(wsb),
        root                     -> rootRoutes,
      )
    ).orNotFound
}
