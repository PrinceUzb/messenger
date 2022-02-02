package uz.scala.messenger.db.algebras

case class Algebras[F[_]](
  user: UserAlgebra[F]
)
