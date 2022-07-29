package uz.scala.messenger.utils.derevo

import derevo.{ Derivation, NewTypeDerivation }

import scala.annotation.implicitNotFound

trait Derive[F[_]] extends Derivation[F] with NewTypeDerivation[F] {
  def instance(implicit ev: OnlyNewtypes): Nothing = ev.absurd

  @implicitNotFound("Only newtypes instances can be derived")
  final abstract class OnlyNewtypes {
    def absurd: Nothing = ???
  }
}
