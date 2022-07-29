package uz.scala.messenger

import ciris.ConfigDecoder
import org.http4s.Uri
import uz.scala.messenger.domain.AppEnv

package object config {
  implicit val UriConfigDecoder: ConfigDecoder[String, Uri] =
    ConfigDecoder[String].mapOption("Uri") { uri =>
      Uri.fromString(uri).toOption
    }

  implicit val AppEnvConfigDecoder: ConfigDecoder[String, AppEnv] =
    ConfigDecoder[String].mapOption("AppEnv") { env =>
      AppEnv.find(env)
    }
}
