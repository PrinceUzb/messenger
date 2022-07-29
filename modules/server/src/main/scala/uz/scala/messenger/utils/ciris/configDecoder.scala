package uz.scala.messenger.utils.ciris

import ciris.ConfigDecoder
import uz.scala.messenger.utils.derevo.Derive

object configDecoder extends Derive[Decoder.Id]

object Decoder {
  type Id[A] = ConfigDecoder[String, A]
}
