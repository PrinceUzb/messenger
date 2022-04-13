package uz.scala.messenger

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Application extends IOApp.Simple {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = MessengerServer.run[IO]
}
