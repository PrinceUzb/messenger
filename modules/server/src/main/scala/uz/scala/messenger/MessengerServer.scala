package uz.scala.messenger

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import uz.scala.messenger.config.{ConfigLoader, HttpServerConfig}
import uz.scala.messenger.modules._
import uz.scala.messenger.services.redis.RedisClient
import eu.timepit.refined.auto.autoUnwrap
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext.global

object MessengerServer {

  def run[F[_]: Async: Console: Logger]: F[ExitCode] =
    for {
      conf     <- ConfigLoader.app[F]
      db       <- LiveDatabase[F](conf.dbConfig)
      redis    <- RedisClient[F](conf.redisConfig)
      programs <- MessengerProgram[F](db, redis)
      httpAPI  <- HttpApi[F](programs, conf.logConfig)
      _        <- server[F](conf.serverConfig, httpAPI.httpApp)
    } yield ExitCode.Success

  private[this] def server[F[_]: Async](
    conf: HttpServerConfig,
    httpApp: WebSocketBuilder2[F] => HttpApp[F]
  ): F[Unit] =
    BlazeServerBuilder[F]
      .withExecutionContext(global)
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(httpApp)
      .serve
      .compile
      .drain
}