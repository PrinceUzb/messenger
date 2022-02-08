package uz.scala.messenger.security

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder}
import org.http4s._
import tsec.authentication._
import tsec.cipher.symmetric.jca._
import tsec.common.SecureRandomId
import uz.scala.messenger.domain.custom.refinements.EmailAddress

object AuthHelper {

  def parseSameSite: PartialFunction[String, SameSite] = {
    case "Lax"    => SameSite.Lax
    case "Strict" => SameSite.Strict
    case "None"   => SameSite.None
    case other    => throw new IllegalArgumentException(s"Argument doesn't match [$other]")
  }

  implicit val encSecureRandomId: Encoder[SecureRandomId] = Encoder.encodeString.contramap(identity)
  implicit val decSecureRandomId: Decoder[SecureRandomId] = Decoder.decodeString.map(SecureRandomId.apply)
  implicit val encSameSite: Encoder[SameSite]             = Encoder.encodeString.contramap(_.renderString)
  implicit val decSameSite: Decoder[SameSite]             = Decoder.decodeString.map(parseSameSite)

  implicit def encBearerToken[T: Encoder]: Encoder[TSecBearerToken[T]] = deriveEncoder[TSecBearerToken[T]]
  implicit def decBearerToken[T: Decoder]: Decoder[TSecBearerToken[T]] = deriveDecoder[TSecBearerToken[T]]
  implicit def bearerTokenCodec[T: Encoder: Decoder]: Codec[TSecBearerToken[T]] =
    Codec.from(decBearerToken[T], encBearerToken[T])

  implicit def encEncryptedCookie[A, T: Encoder]: Encoder[AuthEncryptedCookie[A, T]] =
    deriveEncoder[AuthEncryptedCookie[A, T]]
  implicit def decEncryptedCookie[A, T: Decoder]: Decoder[AuthEncryptedCookie[A, T]] =
    deriveDecoder[AuthEncryptedCookie[A, T]]
  implicit def encryptedCookieCodec[A, T: Encoder: Decoder]: Codec[AuthEncryptedCookie[A, T]] =
    Codec.from(decEncryptedCookie[A, T], encEncryptedCookie[A, T])

  implicit def encEncryptedCookie[A, T: Encoder]: Encoder[AuthEncryptedCookie[A, T]] =
    deriveEncoder[AuthEncryptedCookie[A, T]]
  implicit def decEncryptedCookie[A, T: Decoder]: Decoder[AuthEncryptedCookie[A, T]] =
    deriveDecoder[AuthEncryptedCookie[A, T]]
  implicit def encryptedCookieCodec[A, T: Encoder: Decoder]: Codec[AuthEncryptedCookie[A, T]] =
    Codec.from(decEncryptedCookie[A, T], encEncryptedCookie[A, T])

  type TokenSecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, TSecBearerToken[EmailAddress]]
  type SecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, AuthEncryptedCookie[AES128GCM, EmailAddress]]

  type TokenSecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, TSecBearerToken[EmailAddress]], F[Response[F]]]

  type SecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, AuthEncryptedCookie[AES128GCM, EmailAddress]], F[Response[F]]]

  type OnNotAuthenticated[F[_]] = PartialFunction[Request[F], F[Response[F]]]

}
