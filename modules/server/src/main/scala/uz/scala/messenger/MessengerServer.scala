package uz.scala.messenger

import cats.effect._
import cats.effect.std.{Console, Queue}
import cats.implicits._
import eu.timepit.refined.auto.autoUnwrap
import fs2.concurrent.Topic
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import uz.scala.messenger.config.{ConfigLoader, HttpServerConfig}
import uz.scala.messenger.domain.Message
import uz.scala.messenger.modules._
import uz.scala.messenger.services.redis.RedisClient

import scala.concurrent.ExecutionContext.global

object MessengerServer {

  def run[F[_]: Async: Console: Logger]: F[ExitCode] =
    for {
      conf     <- ConfigLoader.app[F]
      db       <- Database[F](conf.dbConfig)
      redis    <- RedisClient[F](conf.redisConfig)
      queue    <- Queue.unbounded[F, Message]
      topic    <- Topic[F, Message]
      wsStream  = fs2.Stream.fromQueueUnterminated(queue).through(topic.publish)
      programs <- MessengerProgram[F](db, redis, queue)
      httpAPI  <- HttpApi[F](programs, topic, conf.logConfig)
      _        <- server[F](conf.serverConfig, httpAPI.httpApp, wsStream)
    } yield ExitCode.Success

  private[this] def server[F[_]: Async](
    conf: HttpServerConfig,
    httpApp: WebSocketBuilder2[F] => HttpApp[F],
    wsStream: fs2.Stream[F, Nothing]
  ): F[Unit] = {

    BlazeServerBuilder[F]
      .withExecutionContext(global)
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(httpApp)
      .serve.concurrently(wsStream)
      .compile
      .drain
  }
}
