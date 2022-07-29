package uz.scala.messenger.domain.custom

import eu.timepit.refined.api.{ Refined, RefinedTypeOps }
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.{ MatchesRegex, Uri }

package object refinements {
  private type PasswordPred =
    MatchesRegex["^(?=.*[0-9])(?=.*[!@#$%^&*])(?=.*[A-Z])[a-zA-Z0-9!@#$%^&*]{6,32}$"]
  private type TelNumberPred = MatchesRegex["^[+][0-9]{12}$"]

  type Password = String Refined (NonEmpty And PasswordPred)
  object Password extends RefinedTypeOps[Password, String]

  type Tel = String Refined TelNumberPred
  object Tel extends RefinedTypeOps[Tel, String]

  type UriAddress = String Refined Uri
  object UriAddress extends RefinedTypeOps[UriAddress, String]
}
