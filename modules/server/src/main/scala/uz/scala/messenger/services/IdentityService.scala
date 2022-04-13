package uz.scala.messenger.services

import cats.data.OptionT
import cats.effect.Sync
import tsec.authentication.IdentityStore
import tsec.authentication.credentials.SCryptPasswordStore
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt
import uz.scala.messenger.db.algebras.IdentityProvider
import uz.scala.messenger.domain.custom.refinements.EmailAddress

trait IdentityService[F[_], U] extends IdentityStore[F, EmailAddress, U] {
  def get(id: EmailAddress): OptionT[F, U]
  def credentialStore: SCryptPasswordStore[F, EmailAddress]
}

object IdentityService {
  def apply[F[_], U](
    identityProvider: IdentityProvider[F, U]
  )(implicit F: Sync[F]): IdentityService[F, U] = new IdentityService[F, U] {
    override def get(id: EmailAddress): OptionT[F, U] =
      OptionT(identityProvider.findByEmail(id))

    override def credentialStore: SCryptPasswordStore[F, EmailAddress] =
      new SCryptPasswordStore[F, EmailAddress] {
        def retrievePass(id: EmailAddress): OptionT[F, PasswordHash[SCrypt]] =
          OptionT(identityProvider.retrievePass(id))
      }

  }
}
