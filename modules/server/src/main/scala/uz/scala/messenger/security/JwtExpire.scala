package uz.scala.messenger.security

import cats.effect.Sync
import cats.syntax.all._
import uz.scala.messenger.effects.JwtClock
import uz.scala.messenger.types.TokenExpiration
import pdi.jwt.JwtClaim

trait JwtExpire[F[_]] {
  def expiresIn(claim: JwtClaim, exp: TokenExpiration): F[JwtClaim]
}

object JwtExpire {
  def apply[F[_]: Sync]: F[JwtExpire[F]] =
    JwtClock[F].utc.map { implicit jClock => (claim: JwtClaim, exp: TokenExpiration) =>
      Sync[F].delay(claim.issuedNow.expiresIn(exp.value.toMillis))
    }
}
