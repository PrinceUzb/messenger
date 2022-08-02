package uz.scala.messenger

import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import uz.scala.messenger.domain.Message.SendMessage
import uz.scala.messenger.test_utils.Arbitraries.arbNES
import uz.scala.messenger.types.{ Content, UserId }

import java.util.UUID

package object generators extends GeneratorSyntax {
  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  val userIdGen: Gen[UserId] = idGen(UserId.apply)

  val contentGen: Gen[Content] =
    arbitrary[NonEmptyString].map(Content.apply)

  def sendMessageGen(receiverId: Option[UserId] = None): Gen[SendMessage] =
    for {
      c <- contentGen
      i <- userIdGen
    } yield SendMessage(c, receiverId.getOrElse(i))
}
