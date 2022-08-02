package uz.scala.messenger.test_utils

import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.{ Arbitrary, Gen }

object Arbitraries {
  def nonEmptyStringGen(min: Int, max: Int): Gen[String] =
    Gen
      .chooseNum(min, max)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  implicit lazy val arbNES: Arbitrary[NonEmptyString] = Arbitrary(
    nonEmptyStringGen(4, 15).map(NonEmptyString.unsafeFrom)
  )
}
