package uz.scala.messenger.security

import cats.Monad
import cats.syntax.all._
import uz.scala.messenger.effects.GenUUID
import uz.scala.messenger.implicits.GenericTypeOps
import uz.scala.messenger.types.{ JwtAccessTokenKeyConfig, TokenExpiration }
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import pdi.jwt._

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtAccessTokenKeyConfig,
      exp: TokenExpiration,
    ): Tokens[F] =
    new Tokens[F] {
      def create: F[JwtToken] =
        for {
          uuid <- GenUUID[F].make
          claim <- jwtExpire.expiresIn(JwtClaim(uuid.toJson), exp)
          secretKey = JwtSecretKey(config.secret)
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}
