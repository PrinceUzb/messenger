package uz.scala.messenger.modules

import cats.effect._
import cats.effect.std.Console
import eu.timepit.refined.auto.autoUnwrap
import natchez.Trace.Implicits.noop
import skunk._
import skunk.util.Typer
import uz.scala.messenger.config.DBConfig
import uz.scala.messenger.db.algebras.{MessageAlgebra, UserAlgebra}

trait Database[F[_]] {
  val user: UserAlgebra[F]
  val message: MessageAlgebra[F]
}

object Database {
  def apply[F[_]: Async: Console](session: Resource[F, Session[F]]): Database[F] = new Database[F] {
    implicit val ev: Resource[F, Session[F]]  = session
    override val user: UserAlgebra[F]       = UserAlgebra[F]
    override val message: MessageAlgebra[F] = MessageAlgebra[F]
  }
}
