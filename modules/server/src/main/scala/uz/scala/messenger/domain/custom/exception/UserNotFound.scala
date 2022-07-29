package uz.scala.messenger.domain.custom.exception

import uz.scala.messenger.domain.custom.refinements.Tel

import scala.util.control.NoStackTrace

case class UserNotFound(phone: Tel) extends NoStackTrace
