package uz.scala.messenger.test.api

import cats.data._
import cats.effect._
import cats.implicits._
import uz.scala.messenger.domain._
import uz.scala.messenger.domain.custom.refinements.EmailAddress
import uz.scala.messenger.implicits.OptionIdOps
import uz.scala.messenger.routes._
import uz.scala.messenger.security.{AuthService, LiveAuthService}
import uz.scala.messenger.services.redis.RedisClient
import uz.scala.messenger.services.{IdentityService, UserService}
import uz.scala.messenger.test.utils.{FakeData, TestEnv}
import org.http4s.implicits._
import org.http4s.{Method, Request, Response}
import org.typelevel.log4cats.Logger
import tsec.authentication.credentials.SCryptPasswordStore
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

class UserRoutesChecker[F[_]: Async: Logger](implicit F: Sync[F], redisClient: RedisClient[F]) extends TestEnv {

  class FakeIdentityService(isCorrect: Boolean) extends IdentityService[F, User] {

    override def get(id: EmailAddress): OptionT[F, User] =
      OptionT(F.delay(FakeData.user().toOptWhen(isCorrect)))

    override def credentialStore: SCryptPasswordStore[F, EmailAddress] =
      new SCryptPasswordStore[F, EmailAddress] {
        override def retrievePass(id: EmailAddress): OptionT[F, PasswordHash[SCrypt]] =
          OptionT(SCrypt.hashpw[F]("Secret1!").map(_.toOptWhen(isCorrect)))
      }
  }

  class FakeUserService(isCorrect: Boolean) extends UserService[F] {
    override def create(usrData: UserData): F[User] =
      if (isCorrect)
        FakeData.user(usrData.email).pure[F]
      else
        F.raiseError(new Exception("Error"))
  }

  private def reqUserRoutes(
    request: Request[F],
    isCorrect: Boolean = true
  )(implicit authService: AuthService[F, User]): F[Response[F]] =
    UserRoutes[F](
      new FakeUserService(isCorrect)
    ).routes.orNotFound(request)

  def reqToAuth(method: Method, body: Option[Credentials], isCorrect: Boolean): F[Response[F]] =
    for {
      authService <- LiveAuthService[F, User](new FakeIdentityService(isCorrect))
      response    <- reqUserRoutes(Request[F](method, uri"/user/login").withEntity(body))(authService)
    } yield response

  private def reqToAuth(isCorrect: Boolean): F[(AuthService[F, User], Response[F])] =
    for {
      authService <- LiveAuthService[F, User](new FakeIdentityService(isCorrect))

      credentials = Credentials(FakeData.randomEmail, FakeData.Pass)
      loginReq    = Request[F](Method.POST, uri"/user/login").withEntity(credentials)
      authRes <- reqUserRoutes(loginReq)(authService)
    } yield (authService, authRes)

  def reqToUserRegister(method: Method, body: Option[UserData]): F[Response[F]] =
    for {
      authService <- LiveAuthService[F, User](new FakeIdentityService(false))
      request = Request[F](method, uri"/user/register").withEntity(body)
      authRes <- reqUserRoutes(request)(authService)
    } yield authRes

  def reqToGetUser(method: Method, isAuthed: Boolean): F[Response[F]] =
    for {
      res <- reqToAuth(isAuthed)
      (authService, authRes) = res
      request = Request[F](method, uri"/user")
      optCookie = authRes.cookies.find(_.name == "tsec-auth-cookie")
      requestWithCookie = optCookie.fold(request)(cookie => request.addCookie(cookie.name, cookie.content))
      response <- reqUserRoutes(requestWithCookie)(authService)
    } yield response

  def reqToLogout(method: Method, isAuthed: Boolean): F[Response[F]] =
    for {
      res <- reqToAuth(isAuthed)
      (authService, authRes) = res
      request = Request[F](method, uri"/user/logout")
      optCookie = authRes.cookies.find(_.name == "tsec-auth-cookie")
      requestWithCookie = optCookie.fold(request)(cookie => request.addCookie(cookie.name, cookie.content))
      response <- reqUserRoutes(requestWithCookie)(authService)
    } yield response
}
