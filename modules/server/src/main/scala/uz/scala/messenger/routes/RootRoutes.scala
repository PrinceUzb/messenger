package uz.scala.messenger.routes

import cats.effect.Async
import cats.implicits.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import tsec.authentication.asAuthed
import uz.scala.messenger.domain.User
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.utils.FileLoader

object RootRoutes {
  def apply[F[_]: Async: Logger](implicit authService: AuthService[F, User]): RootRoutes[F] =
    new RootRoutes[F]
}

class RootRoutes[F[_]: Async: Logger](implicit authService: AuthService[F, User]) {
  private[this] val supportedStaticExtensions = List(".css", ".png", ".ico", ".js", ".jpg", ".jpeg", ".otf", ".ttf", ".woff2", ".woff")

  implicit object dsl extends Http4sDsl[F]; import dsl._

  private[this] val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "login" => FileLoader[F].page("login.html", request)
    case request if supportedStaticExtensions.exists(request.pathInfo.toString.endsWith) =>
      FileLoader[F].assets(request.pathInfo.toString, request)
  }

  private[this] val privateRoutes: HttpRoutes[F] = authService.securedRoutes( {
    case secReq @ GET -> Root asAuthed _ => FileLoader[F].page("index.html", secReq.request)
  }, request =>
    FileLoader[F].page("login.html", request)
  )

  val routes: HttpRoutes[F] = publicRoutes <+> privateRoutes

}
