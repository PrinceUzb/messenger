package uz.scala.messenger.services

import cats.effect.Sync
import uz.scala.messenger.db.algebras.UserAlgebra
import uz.scala.messenger.domain.{User, UserData}
import org.typelevel.log4cats.Logger

trait UserService[F[_]] {
  def create(userData: UserData): F[User]
  def getAll: F[List[User]]
}

object LiveUserService {
  def apply[F[_]: Logger](
    userAlgebra: UserAlgebra[F]
  )(implicit F: Sync[F]): F[LiveUserService[F]] =
    F.delay(
      new LiveUserService[F](userAlgebra)
    )
}

final class LiveUserService[F[_]: Logger](
  userAlgebra: UserAlgebra[F]
)(implicit F: Sync[F])
    extends UserService[F] {

  override def create(userData: UserData): F[User] =
    userAlgebra.create(userData)

  override def getAll: F[List[User]] = userAlgebra.getAll
}
