package uz.scala.messenger.routes

import cats.effect.{Async, Sync}
import cats.implicits.toSemigroupKOps
import org.http4s.{HttpRoutes, Response, Status, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.log4cats.Logger
import tsec.authentication.asAuthed
import uz.scala.messenger.domain.User
import uz.scala.messenger.security.AuthService
import uz.scala.messenger.utils.FileLoader

object RootRoutes {
  def apply[F[_]: Async: Sync: Logger](implicit authService: AuthService[F, User]): RootRoutes[F] =
    new RootRoutes[F]
}

class RootRoutes[F[_]: Async: Logger](implicit authService: AuthService[F, User], F: Sync[F]) {
  private[this] val supportedStaticExtensions = List(".css", ".png", ".ico", ".js", ".jpg", ".jpeg", ".otf", ".ttf", ".woff2", ".woff")

  implicit object dsl extends Http4sDsl[F]; import dsl._

  private[this] val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request @ GET -> Root / "login" => FileLoader[F].page("login.html", request)
    case request if supportedStaticExtensions.exists(request.pathInfo.toString.endsWith) =>
      FileLoader[F].assets(request.pathInfo.toString, request)
  }

  private[this] val privateRoutes: HttpRoutes[F] = authService.securedRoutes( {
    case secReq @ GET -> Root asAuthed _ =>
      FileLoader[F].page("index.html", secReq.request)
  }, request => {
    println(request.pathInfo)
    if (request.uri == uri"/")
      SeeOther(Location(Uri.unsafeFromString("/login")))
    else
      F.delay(Response[F](Status.Unauthorized))
  })

  val routes: HttpRoutes[F] = publicRoutes <+> privateRoutes

}
