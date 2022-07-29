package uz.scala.messenger.domain

import uz.scala.messenger.domain.custom.refinements.{ Password, Tel }
import derevo.cats.show
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import io.circe.refined._
import eu.timepit.refined.cats._

@derive(decoder, encoder, show)
case class Credentials(phone: Tel, password: Password)
