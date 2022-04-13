package uz.scala.messenger.services.redis

import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import uz.scala.messenger.implicits._
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effects.{SetArg, SetArgs}
import io.circe.Codec
import tsec.authentication.BackingStore

trait RedisClient[F[_]] {
  def secretKeyStore: TsecSecretKeyStore[F]

  def dummyBackingStore[I, V: Codec](getId: V => I): BackingStore[F, I, V]
}

object RedisClient {
  def apply[F[_]: Async](redis: RedisCommands[F, String, String]): RedisClient[F] = new RedisClient[F] {

    override def secretKeyStore: TsecSecretKeyStore[F] = TsecSecretKeyStore[F](redis)

    override def dummyBackingStore[I, V: Codec](
      getId: V => I
    ): BackingStore[F, I, V] = new BackingStore[F, I, V] {

      override def put(elem: V): F[V] =
        redis
          .setNx(getId(elem).toString, elem.toJson)
          .map { result =>
            if (result) elem else throw new IllegalArgumentException
          }

      override def get(id: I): OptionT[F, V] =
        OptionT {
          redis.get(id.toString).map(_.map(_.as[V]))
        }

      override def update(v: V): F[V] =
        redis
          .set(getId(v).toString, v.toJson, SetArgs(SetArg.Existence.Xx))
          .map { result =>
            if (result) v else throw new IllegalArgumentException
          }

      override def delete(id: I): F[Unit] =
        redis.del(id.toString).map { result =>
          if (result == 1) () else throw new IllegalArgumentException
        }
    }
  }
}
