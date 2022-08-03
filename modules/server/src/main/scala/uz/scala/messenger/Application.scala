package uz.scala.messenger

import cats.effect.implicits.genSpawnOps
import cats.effect.std.{ Queue, Supervisor }
import cats.effect.{ Async, IO, IOApp, Resource }
import cats.implicits._
import dev.profunktor.redis4cats.log4cats._
import fs2.concurrent.Topic
import org.http4s.HttpApp
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{ Logger, SelfAwareStructuredLogger }
import skunk.Session
import uz.scala.messenger.config.{ AppConfig, ConfigLoader, HttpServerConfig }
import uz.scala.messenger.domain.Message
import uz.scala.messenger.modules.{ HttpApi, Security, Services }
import uz.scala.messenger.resources.{ AppResources, MkHttpServer }

object Application extends IOApp.Simple {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  private def makeHttpApp[F[_]: Async: Logger](
      cfg: AppConfig
    )(
      res: AppResources[F]
    ): F[(HttpServerConfig, WebSocketBuilder2[F] => HttpApp[F])] = {
    implicit val session: Resource[F, Session[F]] = res.postgres
    val services = Services[F](res.redis)
    for {
      queue <- Queue.unbounded[F, Message]
      topic <- Topic[F, Message]
      wsStream = fs2.Stream.fromQueueUnterminated(queue).through(topic.publish)
      _ <- wsStream.compile.drain.start.void
      security <- Security[F](cfg, services.users, res.redis)
    } yield cfg.serverConfig -> HttpApi[F](
      security,
      services,
      res.redis,
      topic,
      queue,
      cfg.logConfig,
    ).httpApp
  }

  override def run: IO[Unit] =
    ConfigLoader.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") >>
        Supervisor[IO].flatMap { implicit sp =>
          AppResources
            .make[IO](cfg)
            .evalMap(makeHttpApp(cfg))
            .flatMap {
              case (cfg, httpApp) =>
                MkHttpServer[IO].newEmber(cfg, httpApp)
            }

        }.useForever
    }
}
