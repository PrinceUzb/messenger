package uz.scala.messenger.routes

import cats.effect._
import cats.implicits._
import uz.scala.messenger.domain._
import uz.scala.messenger.implicits.ResponseIdOps
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.services.UserService
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.multipart.Multipart
import org.typelevel.log4cats.Logger
import tsec.authentication._
import tsec.authentication.credentials.CredentialsError
import uz.scala.messenger.domain.custom.refinements.EmailAddress
import uz.scala.messenger.utils._

object UserRoutes {
  val prefixPath = "/user"
  def apply[F[_]: Async: Logger](userService: UserService[F])(implicit
    authService: AuthService[F, User]
  ): UserRoutes[F] = new UserRoutes(userService)
}

final class UserRoutes[F[_]: Async](userService: UserService[F])(implicit
  logger: Logger[F],
  authService: AuthService[F, User]
) {

  implicit object dsl extends Http4sDsl[F]
  import dsl._

  private[this] val loginRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      req.decode[Multipart[F]] { multipart =>
        authService
          .authorizer(multipart)
          .recoverWith {
            case _: CredentialsError =>
              SeeOther(Location(Uri.unsafeFromString("/"))).map {
                _.flashing(Error, "Something went wrong. Please try again!")
              }.flatTap(a=>logger.debug(a.toString()))
            case error =>
              logger.error(error)(s"Error occurred while authorization. Error:") >>
                SeeOther(Location(Uri.unsafeFromString("/"))).map {
                  _.flashing(Error, "Something went wrong. Please try again!")
                }.flatTap(a=>println(a.toString()).pure[F])
          }
      }

    case req @ POST -> Root / "register" =>
      (for {
        userData <- req.as[UserData]
        _        <- logger.debug(s"$userData")
        user     <- userService.create(userData)
        response <- Created(user)
      } yield response)
        .handleErrorWith { err =>
          logger.error(err)("Error occurred while register User. ") >>
            BadRequest("Something went wrong. Please try again!")
        }

    case GET -> Root / "m" / email =>
      authService.get(EmailAddress.unsafeFrom(email)).semiflatMap(Ok(_)).getOrElseF(BadRequest("error"))
  }

  private[this] val privateRoutes: HttpRoutes[F] = authService.securedRoutes {
    case GET -> Root asAuthed user =>
      Ok(user)

    case secureReq @ GET -> Root / "logout" asAuthed _ =>
      authService.discard(secureReq.authenticator)

  }

  val routes: HttpRoutes[F] = loginRoutes <+> privateRoutes
}
