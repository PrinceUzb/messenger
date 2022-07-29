package uz.scala.messenger.services

import cats.implicits._
import uz.scala.messenger.domain.User.UserStatus
import uz.scala.messenger.domain.custom.refinements.Tel
import uz.scala.messenger.types._
import eu.timepit.refined.types.string.NonEmptyString
import skunk._
import skunk.codec.all._
import skunk.data.Type
import skunk.implicits._
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

package object sql {
  val nes: Codec[NonEmptyString] = varchar.imap[NonEmptyString](NonEmptyString.unsafeFrom)(_.value)

  val username: Codec[Username] = nes.imap[Username](Username.apply)(_.value)

  val passwordHash: Codec[PasswordHash[SCrypt]] =
    varchar.imap[PasswordHash[SCrypt]](PasswordHash[SCrypt])(_.toString)

  val status: Codec[UserStatus] = `enum`[UserStatus](_.value, UserStatus.find, Type("role"))

  val tel: Codec[Tel] = varchar.imap[Tel](Tel.unsafeFrom)(_.value)

  final implicit class FragmentOps(af: AppliedFragment) {
    def paginate(lim: Int, index: Int): AppliedFragment = {
      val offset = (index - 1) * lim
      val filter: Fragment[Int ~ Int] = sql" LIMIT $int4 OFFSET $int4 "
      af |+| filter(lim ~ offset)
    }

    /** Returns `WHERE (f1) AND (f2) AND ... (fn)` for defined `f`, if any, otherwise the empty fragment. */
    def whereAndOpt(fs: List[AppliedFragment]): AppliedFragment = {
      val filters =
        if (fs.isEmpty)
          AppliedFragment.empty
        else
          fs.foldSmash(void" WHERE ", void" AND ", AppliedFragment.empty)
      af |+| filters
    }

    def andOpt(fs: List[AppliedFragment]): AppliedFragment = {
      val filters =
        if (fs.isEmpty)
          AppliedFragment.empty
        else
          fs.foldSmash(void" AND ", void" AND ", AppliedFragment.empty)
      af |+| filters
    }
  }
}
