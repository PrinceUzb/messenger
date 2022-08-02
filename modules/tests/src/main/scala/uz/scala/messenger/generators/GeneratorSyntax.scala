package uz.scala.messenger.generators

import cats.effect.Sync
import org.scalacheck.Gen

trait GeneratorSyntax {
  implicit def genSyntax[T](generator: Gen[T]): GenSyntax[T] = new GenSyntax(generator)
}

class GenSyntax[T](generator: Gen[T]) {
  def sync[F[_]: Sync]: F[T] = Sync[F].delay(get)
  def get: T = generator.sample.get
}
