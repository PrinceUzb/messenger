package uz.scala.messenger.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import skunk.Session
import skunk.implicits.toIdOps
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.scala.messenger.db.sql.UserSql._
import uz.scala.messenger.domain.custom.refinements.EmailAddress
import uz.scala.messenger.domain.{User, UserData}
import uz.scala.messenger.utils.GenUUID

trait UserAlgebra[F[_]] extends IdentityProvider[F, User] {
  def findByEmail(email: EmailAddress): F[Option[User]]
  def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]]
  def create(user: UserData): F[User]
  def getAll: F[List[User]]
}

object UserAlgebra {
  def apply[F[_]: Sync](implicit session: Resource[F, Session[F]]): UserAlgebra[F] = new UserAlgebra[F]
    with SkunkHelper[F] {

    override def findByEmail(email: EmailAddress): F[Option[User]] = prepOptQuery(selectByEmail, email)

    override def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]] =
      prepOptQuery(selectPass, email).map(_.map(PasswordHash[SCrypt]))

    override def create(userData: UserData): F[User] =
      GenUUID[F].make.flatMap { uuid =>
        prepQueryUnique(insert, uuid ~ userData)
      }
    override def getAll: F[List[User]] = prepAllQuery(selectAll)
  }

}
