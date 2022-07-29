package uz.scala.messenger

import cats.effect.std.Supervisor
import cats.effect.{ IO, IOApp, Resource }
import uz.scala.messenger.resources.{ AppResources, MkHttpServer }
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{ Logger, SelfAwareStructuredLogger }
import skunk.Session
import uz.scala.messenger.config.ConfigLoader
import uz.scala.messenger.modules.{ HttpApi, Services }

object Application extends IOApp.Simple {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigLoader.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") >>
        Supervisor[IO].use { implicit sp =>
          AppResources
            .make[IO](cfg)
            .evalMap { res =>
              implicit val session: Resource[IO, Session[IO]] = res.postgres

              val services = Services[IO](res.redis)
              modules.Security[IO](cfg, services.users, res.redis).map { security =>
                cfg.serverConfig -> HttpApi[IO](
                  security,
                  services,
                  res.redis,
                  cfg.logConfig,
                ).httpApp
              }
            }
            .flatMap {
              case (cfg, httpApp) =>
                MkHttpServer[IO].newEmber(cfg, httpApp)
            }
            .useForever
        }
    }
}
