package uz.scala.messenger.modules

import cats.effect._
import cats.implicits._
import uz.scala.messenger.config.LogConfig
import uz.scala.messenger.domain.User
import uz.scala.messenger.routes._
import uz.scala.messenger.security.AuthService
import org.http4s.{HttpApp, _}
import org.http4s.implicits._
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.{Router, middleware}
import org.typelevel.log4cats.Logger

object HttpApi {
  def apply[F[_]: Async: Logger](
    program: MessengerProgram[F],
    logConfig: LogConfig
  )(implicit F: Sync[F]): F[HttpApi[F]] =
    F.delay(
      new HttpApi[F](program, logConfig)
    )
}

final class HttpApi[F[_]: Async: Logger] private (
  program: MessengerProgram[F],
  logConfig: LogConfig
) {
  private[this] val root: String = "/"

  implicit val authUser: AuthService[F, User] = program.auth.user

  private[this] val rootRoutes: HttpRoutes[F] = RootRoutes[F]
  private[this] val userRoutes: HttpRoutes[F] = UserRoutes[F](program.userService).routes
  private[this] val messageRoutes: WebSocketBuilder2[F] => HttpRoutes[F] = wsb => MessageRoutes[F].routes(wsb)
  private[this] val webjars: HttpRoutes[F]    = webjarServiceBuilder[F].toRoutes

  private[this] val loggedRoutes: HttpRoutes[F] => HttpRoutes[F] = http =>
    middleware.Logger.httpRoutes(logConfig.httpHeader, logConfig.httpBody)(http)

  val httpApp: WebSocketBuilder2[F] => HttpApp[F] = wsb =>
    loggedRoutes(
      Router (
        "/" -> rootRoutes,
        UserRoutes.prefixPath -> userRoutes,
        MessageRoutes.prefixPath -> messageRoutes(wsb),
        "/webjars" -> webjars
      )
    ).orNotFound
}
