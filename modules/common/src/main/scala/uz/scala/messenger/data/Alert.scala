package uz.scala.messenger.data

sealed trait Alert {
  def value: String = this.toString.toLowerCase
}

object Alert {
  case object Info    extends Alert
  case object Error   extends Alert
  case object Warning extends Alert
  case object Success extends Alert
}
