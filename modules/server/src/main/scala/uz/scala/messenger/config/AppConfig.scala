package uz.scala.messenger.config

import uz.scala.messenger.domain.AppEnv

case class AppConfig(
    env: AppEnv,
    jwtConfig: JwtConfig,
    dbConfig: DBConfig,
    redis: RedisConfig,
    serverConfig: HttpServerConfig,
    logConfig: LogConfig,
  )
