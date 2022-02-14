package uz.scala.messenger

package object utils {
  sealed trait AlertLevel {
    def value: String = this.toString.toLowerCase
  }
  case object Info    extends AlertLevel
  case object Error   extends AlertLevel
  case object Success extends AlertLevel
  case object Warning extends AlertLevel

}
