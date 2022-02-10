package uz.scala.messenger.implicits
import eu.timepit.refined.types.string.NonEmptyString
import org.http4s.Response
import uz.scala.messenger.data.Alert

final class ResponseOps[F[_]](response: Response[F]) {
  def flashing(values: (Alert, NonEmptyString)*) = response.addCookie(Flash(values.toMap))

}
