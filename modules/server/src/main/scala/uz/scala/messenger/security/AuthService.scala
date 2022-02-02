package uz.scala.messenger.security

import cats.effect._
import cats.implicits._
import uz.scala.messenger.domain.Credentials
import uz.scala.messenger.domain.custom.refinements.EmailAddress
import uz.scala.messenger.implicits.PartOps
import uz.scala.messenger.security.AuthHelper._
import uz.scala.messenger.services.IdentityService
import uz.scala.messenger.services.redis.RedisClient
import eu.timepit.refined.auto.autoUnwrap
import io.circe.refined._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.multipart.Multipart
import org.http4s.{HttpRoutes, Request, Response, Status, Uri}
import tsec.authentication._
import tsec.authentication.credentials.RawCredentials
import tsec.cipher.symmetric.jca.{AES128GCM, SecretKey}
import tsec.cipher.symmetric.{AADEncryptor, IvGen}
import tsec.common.SecureRandomId

import scala.concurrent.duration.DurationInt

trait AuthService[F[_], U] {
  def authorizer(request: Multipart[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]

  def authorizer(request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]

  def securedRoutesWithToken: TokenSecHttpRoutes[F, U] => HttpRoutes[F]

  def securedRoutes: SecHttpRoutes[F, U] => HttpRoutes[F]

  def discard(authenticator: AuthEncryptedCookie[AES128GCM, EmailAddress])(implicit dsl: Http4sDsl[F]): F[Response[F]]
}

object LiveAuthService {
  def apply[F[_]: Async, U](
    identityService: IdentityService[F, U]
  )(implicit F: Sync[F], redisClient: RedisClient[F]): F[AuthService[F, U]] =
    F.delay(
      new LiveAuthService[F, U](identityService)
    )
}

final class LiveAuthService[F[_]: Async, U] private (
  identityService: IdentityService[F, U]
)(implicit F: Sync[F], redisClient: RedisClient[F])
    extends AuthService[F, U] {

  implicit val encryptor: AADEncryptor[F, AES128GCM, SecretKey] = AES128GCM.genEncryptor[F]
  implicit val gcmStrategy: IvGen[F, AES128GCM]                 = AES128GCM.defaultIvStrategy[F]

  private[this] val bearerTokenStore =
    redisClient.dummyBackingStore[SecureRandomId, TSecBearerToken[EmailAddress]](s => SecureRandomId.coerce(s.id))

  private[this] val settings: TSecTokenSettings =
    TSecTokenSettings(
      expiryDuration = 8.hours,
      maxIdle = 30.minutes.some
    )

  private[this] val cookieSetting: TSecCookieSettings =
    TSecCookieSettings(
      secure = true,
      expiryDuration = 8.hours,
      maxIdle = 30.minutes.some
    )

  private[this] val key: SecretKey[AES128GCM] = AES128GCM.unsafeGenerateKey

  private[this] def bearerTokenAuth: BearerTokenAuthenticator[F, EmailAddress, U] =
    BearerTokenAuthenticator(
      bearerTokenStore,
      identityService,
      settings
    )

  private[this] def stateless: StatelessECAuthenticator[F, EmailAddress, U, AES128GCM] =
    EncryptedCookieAuthenticator.stateless(
      cookieSetting,
      identityService,
      key
    )

  private[this] def authWithToken: TokenSecReqHandler[F, U] = SecuredRequestHandler(bearerTokenAuth)

  private[this] def auth: SecReqHandler[F, U] = SecuredRequestHandler(stateless)

  private[this] def verify(Credentials: Credentials): F[Boolean] =
    identityService.credentialStore.isAuthenticated(RawCredentials(Credentials.email, Credentials.password))

  private[this] def createSession(credentials: Credentials): F[Response[F]] = {
    auth.authenticator
      .create(credentials.email)
      .map(auth.authenticator.embed(Response(Status.NoContent), _))
  }

  override def authorizer(request: Multipart[F])(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    for {
      credentials <- request.parts.convert[Credentials]
      isAuthed    <- verify(credentials)
      response <-
        if (isAuthed)
          createSession(credentials)
        else
          Forbidden("Email or password isn't correct")
    } yield response
  }

  override def authorizer(request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    for {
      credentials <- request.as[Credentials]
      isAuthed    <- verify(credentials)
      response <-
        if (isAuthed)
          createSession(credentials)
        else
          Forbidden("Email or password isn't correct")
    } yield response
  }


  override def securedRoutesWithToken: TokenSecHttpRoutes[F, U] => HttpRoutes[F] = pf =>
    authWithToken.liftService(TSecAuthService(pf))

  override def securedRoutes: SecHttpRoutes[F, U] => HttpRoutes[F] = pf => auth.liftService(TSecAuthService(pf))

  override def discard(
    authenticator: AuthEncryptedCookie[AES128GCM, EmailAddress]
  )(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    auth.authenticator.discard(authenticator).flatMap { _ =>
      SeeOther(Location(Uri.unsafeFromString("/login")))
    }
  }
}
