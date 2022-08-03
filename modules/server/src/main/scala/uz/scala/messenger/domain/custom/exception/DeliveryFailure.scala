package uz.scala.messenger.domain.custom.exception

sealed trait DeliveryFailure extends Exception

object DeliveryFailure {
  case object ParseFailure extends DeliveryFailure
}
