package uz.scala.messenger.syntax

import cats.MonadThrow
import cats.syntax.all._
import io.circe.Decoder
import org.http4s.circe.{ JsonDecoder, toMessageSyntax }
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.multipart.Part
import org.http4s.{ MediaType, Request, Response }

trait Http4Syntax {
  implicit def http4SyntaxReqOps[F[_]: JsonDecoder: MonadThrow](
      request: Request[F]
    ): RequestOps[F] =
    new RequestOps(request)
  implicit def http4SyntaxPartOps[F[_]](parts: Vector[Part[F]]): PartOps[F] =
    new PartOps(parts)
}

final class RequestOps[F[_]: JsonDecoder: MonadThrow](private val request: Request[F])
    extends Http4sDsl[F] {
  def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
    request.asJsonDecode[A].attempt.flatMap {
      case Left(e) =>
        Option(e.getCause) match {
          case Some(c) if c.getMessage.startsWith("Predicate") => BadRequest(c.getMessage)
          case _ => UnprocessableEntity()
        }
      case Right(a) => f(a)
    }
}

final class PartOps[F[_]](private val parts: Vector[Part[F]]) {
  private def filterFileTypes(part: Part[F]): Boolean = part.filename.exists(_.trim.nonEmpty)

  def fileParts: Vector[Part[F]] = parts.filter(filterFileTypes)

  def fileParts(mediaType: MediaType): Vector[Part[F]] =
    parts.filter(_.headers.get[`Content-Type`].exists(_.mediaType == mediaType))

  def isFilePartExists: Boolean = parts.exists(filterFileTypes)

  def textParts: Vector[Part[F]] = parts.filterNot(filterFileTypes)
}
