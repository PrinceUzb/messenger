package uz.scala.messenger.services.redis

import cats.data.OptionT
import cats.effect.{Async, Sync}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import dev.profunktor.redis4cats.effect.Log.Stdout._
import dev.profunktor.redis4cats.effects.{SetArg, SetArgs}
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import eu.timepit.refined.auto.autoUnwrap
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder}
import tsec.authentication.BackingStore
import tsec.cipher.symmetric.jca.{AES128GCM, SecretKey}
import uz.scala.messenger.config.RedisConfig
import uz.scala.messenger.implicits.{CirceDecoderOps, GenericTypeOps}

import java.time.Instant
import java.util.Base64
import scala.concurrent.duration.DurationInt

object RedisClient {
  def apply[F[_]: Async](redisConfig: RedisConfig)(implicit F: Sync[F]): F[RedisClient[F]] =
    F.delay(new RedisClient(redisConfig))
}

class RedisClient[F[_]: Async](redisConfig: RedisConfig)(implicit F: Sync[F]) {
  private val RedisService     = Redis[F].utf8(redisConfig.uri)
  private val TSEC_KEY: String = "TSEC_SECRET_KEY"

  /** @param key the tsec SecretKey[A] [[SecretKey]]
    * @param expireAt the expiration time of secret key
    */
  case class TsecSecretKey(key: SecretKey[AES128GCM], expireAt: Instant) {
    def isExpired(now: Instant): Boolean = now.isBefore(expireAt)
  }
  object TsecSecretKey {

    implicit val encSecret: Encoder[SecretKey[AES128GCM]] = Encoder.encodeString.contramap { key =>
      Base64.getEncoder.encodeToString(key.getEncoded)
    }
    implicit val decSecret: Decoder[SecretKey[AES128GCM]] = Decoder.decodeString.map { str =>
      AES128GCM.unsafeBuildKey(Base64.getDecoder.decode(str))
    }
    implicit val dec: Decoder[TsecSecretKey] = deriveDecoder[TsecSecretKey]
    implicit val enc: Encoder[TsecSecretKey] = deriveEncoder[TsecSecretKey]
  }

  /** @param now the current time
    * @param redisCommands the commands redis
    * @return the tsec SecretKey[A] [[SecretKey]]
    */
  def generateKey(now: Instant)(implicit redisCommands: RedisCommands[F, String, String]): F[SecretKey[AES128GCM]] =
    AES128GCM.generateKey[F].flatMap { newKey =>
      val newTecSecretKey = TsecSecretKey(newKey, now.plusSeconds(10.days.toSeconds))
      redisCommands.setEx(TSEC_KEY, newTecSecretKey.toJson, 10.days).map { _ =>
        newKey
      }
    }

  /** @param now the current time
    * @param secretKey the tsec secret key [[TsecSecretKey]]
    * @param redisCommands the commands redis
    * @return the tsec SecretKey[A] [[SecretKey]]
    */
  def validateAndRetrieve(now: Instant, secretKey: TsecSecretKey)(implicit
    redisCommands: RedisCommands[F, String, String]
  ): F[SecretKey[AES128GCM]] =
    if (secretKey.isExpired(now))
      F.delay(secretKey.key)
    else
      generateKey(now)

  def getSecretKey: F[SecretKey[AES128GCM]] =
    RedisService.use { implicit redis =>
      OptionT(redis.get(TSEC_KEY))
        .map(_.as[TsecSecretKey])
        .semiflatMap { secretKey =>
          F.delay(Instant.now()).flatMap(validateAndRetrieve(_, secretKey))
        }
        .getOrElseF(F.delay(Instant.now()).flatMap(generateKey))
    }

  def dummyBackingStore[I, V: Codec](
    getId: V => I
  )(implicit F: Sync[F]): BackingStore[F, I, V] = new BackingStore[F, I, V] {

    override def put(elem: V): F[V] =
      RedisService.use { redis =>
        redis
          .setNx(getId(elem).toString, elem.toJson)
          .map { result =>
            if (result) elem else throw new IllegalArgumentException
          }
      }

    override def get(id: I): OptionT[F, V] =
      OptionT {
        RedisService.use { redis =>
          redis.get(id.toString).map(_.map(_.as[V]))
        }
      }

    override def update(v: V): F[V] =
      RedisService.use { redis =>
        redis
          .set(getId(v).toString, v.toJson, SetArgs(SetArg.Existence.Xx))
          .map { result =>
            if (result) v else throw new IllegalArgumentException
          }
      }

    override def delete(id: I): F[Unit] =
      RedisService.use { redis =>
        redis.del(id.toString).map { result =>
          if (result == 1) () else throw new IllegalArgumentException
        }
      }
  }
}
