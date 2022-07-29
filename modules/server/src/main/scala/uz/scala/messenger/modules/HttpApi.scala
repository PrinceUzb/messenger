package uz.scala.messenger.modules

import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.typelevel.log4cats.Logger
import pdi.jwt.JwtClaim
import uz.scala.messenger.config.LogConfig
import uz.scala.messenger.domain.User
import uz.scala.messenger.implicits.CirceDecoderOps
import uz.scala.messenger.routes._
import uz.scala.messenger.services.redis.RedisClient

import scala.concurrent.duration.DurationInt

object HttpApi {
  def apply[F[_]: Async: Logger](
      security: Security[F],
      services: Services[F],
      redis: RedisClient[F],
      logConfig: LogConfig,
    ): HttpApi[F] =
    new HttpApi[F](security, services, redis, logConfig)
}

final class HttpApi[F[_]: Async: Logger] private (
    security: Security[F],
    services: Services[F],
    redis: RedisClient[F],
    logConfig: LogConfig,
  ) {
  private[this] val baseURL: String = "/"

  def findUser(token: JwtToken): JwtClaim => F[Option[User]] = _ =>
    OptionT(redis.get(token.value))
      .map(_.as[User])
      .value

  private[this] val usersMiddleware =
    JwtAuthMiddleware[F, User](security.userJwtAuth.value, findUser)

  private[this] val authRoutes = AuthRoutes[F](security.auth).routes(usersMiddleware)
  private[this] val userRoutes =
    new UserRoutes[F](services.users).routes(usersMiddleware)

  private[this] val openRoutes: HttpRoutes[F] =
    userRoutes <+> authRoutes

  private[this] val routes: HttpRoutes[F] = Router(
    baseURL -> openRoutes
  )

  private[this] val middleware: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] =>
    AutoSlash(http)
  } andThen { http: HttpRoutes[F] =>
    CORS
      .policy
      .withAllowOriginAll
      .withAllowCredentials(false)
      .apply(http)
  } andThen { http: HttpRoutes[F] =>
    Timeout(60.seconds)(http)
  }

  def httpLogger: Option[String => F[Unit]] = Option(Logger[F].info(_))

  private[this] val loggers: HttpApp[F] => HttpApp[F] = { http: HttpApp[F] =>
    RequestLogger.httpApp(logConfig.httpHeader, logConfig.httpBody, logAction = httpLogger)(http)
  } andThen { http: HttpApp[F] =>
    ResponseLogger.httpApp(logConfig.httpHeader, logConfig.httpBody, logAction = httpLogger)(http)
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}
