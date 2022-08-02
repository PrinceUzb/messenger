package uz.scala.messenger.test_utils

import cats.effect.{ IO, Resource }
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.noop.NoOpLogger
import uz.scala.messenger.types.UserId
import weaver.scalacheck.CheckConfig

import java.util.UUID
import scala.io.Source

trait Container {
  type Res
  lazy val imageName: String = "postgres:12"
  lazy val container: PostgreSQLContainer[Nothing] = new PostgreSQLContainer(
    DockerImageName
      .parse(imageName)
      .asCompatibleSubstituteFor("postgres")
  )

  val JohnId: UserId = UserId(UUID.fromString("451d917e-122d-11ed-861d-0242ac120002"))
  val JaneId: UserId = UserId(UUID.fromString("4b5ab36e-122d-11ed-861d-0242ac120002"))

  val customCheckConfig: CheckConfig = CheckConfig.default.copy(minimumSuccessful = 5)

  implicit val logger: SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  def migrateSql(container: PostgreSQLContainer[Nothing]): Unit = {
    val source = Source.fromFile(getClass.getResource("/tables.sql").getFile)
    val sqlScripts = source.getLines().mkString
    source.close()
    val connection = container.createConnection(
      s"?user=${container.getUsername}&password=${container.getPassword}"
    )
    val stmt = connection.createStatement()
    stmt.execute(sqlScripts)
    stmt.closeOnCompletion()
  }

  val dbResource: Resource[IO, PostgreSQLContainer[Nothing]] =
    for {
      container <- Resource.fromAutoCloseable {
        IO {
          container.start()
          container.setCommand("postgres", "-c", "max_connections=150")
          container
        }
      }
      _ = migrateSql(container)
      _ <- Resource.eval(logger.info("Container has started"))
    } yield container
}
