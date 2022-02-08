package uz.scala.messenger
import cats.effect.SyncIO
import org.typelevel.vault.Key

package object utils {
  sealed trait AlertLevel
  case object Info extends AlertLevel
  case object Error extends AlertLevel
  case object Success extends AlertLevel
  case object Warning extends AlertLevel

  final case class Alert(level: AlertLevel, content: String)
  val FLASH_SESSION: Key[Alert] = Key.newKey[SyncIO, Alert].unsafeRunSync()
}
