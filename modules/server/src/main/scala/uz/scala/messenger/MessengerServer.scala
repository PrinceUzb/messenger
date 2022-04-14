package uz.scala.messenger

import cats.effect._
import cats.effect.std.{Console, Queue, Supervisor}
import cats.implicits._
import dev.profunktor.redis4cats.log4cats._
import eu.timepit.refined.auto.autoUnwrap
import fs2.concurrent.Topic
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import uz.scala.messenger.config.{ConfigLoader, HttpServerConfig}
import uz.scala.messenger.domain.Message
import uz.scala.messenger.implicits._
import uz.scala.messenger.modules._
import uz.scala.messenger.resources.AppResources

object MessengerServer {

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

  private def server[F[_]: Async: Logger](
    conf: HttpServerConfig
  )(httpApp: WebSocketBuilder2[F] => HttpApp[F]): Resource[F, Server] =
    BlazeServerBuilder[F]
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(httpApp)
      .resource
      .evalTap(showEmberBanner[F])

  def run[F[_]: Async: Console: Logger: Network]: F[Unit] =
    ConfigLoader.app[F].flatMap { conf =>
      Supervisor[F]
        .use { implicit sp =>
          AppResources[F](conf)
            .evalMap { res =>
              for {
                queue    <- Queue.unbounded[F, Message]
                topic    <- Topic[F, Message]
                programs <- MessengerProgram[F](Database[F](res.postgres), res.redis, queue)
                httpAPI  <- HttpApi[F](programs, topic, conf.logConfig, queue)
              } yield httpAPI.httpApp
            }
            .flatMap(server[F](conf.serverConfig))
            .useForever_[Unit]
        }
    }
}
