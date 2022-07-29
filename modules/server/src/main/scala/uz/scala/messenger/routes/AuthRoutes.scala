package uz.scala.messenger.routes

import cats.syntax.all._
import cats.{ Monad, MonadThrow }
import dev.profunktor.auth.AuthHeaders
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.typelevel.log4cats.Logger
import uz.scala.messenger
import uz.scala.messenger.domain.User.CreateUser
import uz.scala.messenger.domain.custom.exception.{ InvalidPassword, PhoneInUse, UserNotFound }
import uz.scala.messenger.domain.{ User, tokenCodec }
import uz.scala.messenger.services.Auth

final case class AuthRoutes[F[_]: Monad: JsonDecoder: MonadThrow](
    auth: Auth[F]
  )(implicit
    logger: Logger[F]
  ) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/auth"

  private val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      req.decodeR[messenger.domain.Credentials] { credentials =>
        auth
          .login(credentials)
          .flatMap(Ok(_))
          .recoverWith {
            case UserNotFound(_) | InvalidPassword(_) =>
              Forbidden()
          }
      }

    case req @ POST -> Root / "user" =>
      req.decodeR[CreateUser] { createUser =>
        auth
          .newUser(createUser)
          .flatMap(Created(_))
          .recoverWith {
            case phoneInUseError: PhoneInUse =>
              logger.error(s"Phone is already in use. Error: ${phoneInUseError.phone.value}") >>
                NotAcceptable("Phone is already in use. Please try again with other phone number")
            case error =>
              logger.error(error)("Error occurred creating user!") >>
                BadRequest("Error occurred creating user. Please try again!")
          }
      }

  }

  private[this] val privateRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case ar @ GET -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(auth.logout(_, user.phone)) *> NoContent()

  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> (publicRoutes <+> authMiddleware(privateRoutes))
  )
}
