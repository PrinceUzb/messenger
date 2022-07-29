package uz.scala.messenger.config

import ciris.Secret
import uz.scala.messenger.types.{ JwtAccessTokenKeyConfig, PasswordSalt, TokenExpiration }

case class JwtConfig(
    tokenConfig: Secret[JwtAccessTokenKeyConfig],
    passwordSalt: Secret[PasswordSalt],
    tokenExpiration: TokenExpiration,
  )
