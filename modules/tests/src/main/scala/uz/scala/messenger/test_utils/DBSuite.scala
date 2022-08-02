package uz.scala.messenger.test_utils

import cats.effect.{ IO, Resource }
import natchez.Trace.Implicits.noop
import skunk._
import skunk.codec.all.text
import skunk.implicits._
import skunk.util.Typer
import weaver.IOSuite
import weaver.scalacheck.{ CheckConfig, Checkers }

trait DBSuite extends IOSuite with Checkers with Container {
  type Res = Resource[IO, Session[IO]]

  override def checkConfig: CheckConfig = CheckConfig.default.copy(minimumSuccessful = 1)

  def checkPostgresConnection(
      postgres: Resource[IO, Session[IO]]
    ): IO[Unit] =
    postgres.use { session =>
      session.unique(sql"select version();".query(text)).flatMap { v =>
        logger.info(s"Connected to Postgres $v")
      }
    }

  override def sharedResource: Resource[IO, Res] =
    for {
      container <- dbResource
      session <- Session
        .pooled[IO](
          host = container.getHost,
          port = container.getFirstMappedPort,
          user = container.getUsername,
          password = Some(container.getPassword),
          database = container.getDatabaseName,
          max = 100,
          strategy = Typer.Strategy.SearchPath,
        )
        .evalTap(checkPostgresConnection)
    } yield session
}
