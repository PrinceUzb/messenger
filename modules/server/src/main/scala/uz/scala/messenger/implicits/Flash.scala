package uz.scala.messenger.implicits
import eu.timepit.refined.types.string.NonEmptyString
import uz.scala.messenger.data.Alert

case class Flash(data: Map[Alert, NonEmptyString] = Map.empty) {

  /** Optionally returns the flash value associated with a key.
    */
  def get(key: Alert): Option[NonEmptyString] = data.get(key)

  /** Returns `true` if this flash scope is empty.
    */
  def isEmpty: Boolean = data.isEmpty

  /** Adds a value to the flash scope, and returns a new flash scope.
    *
    * For example:
    * {{{
    * flash + (Success -> "Done!")
    * }}}
    *
    * @param kv
    *   the key-value pair to add
    * @return
    *   the modified flash scope
    */
  def +(kv: (Alert, NonEmptyString)): Flash =
    copy(data + kv)

  /** Removes a value from the flash scope.
    *
    * For example:
    * {{{
    * flash - Success
    * }}}
    *
    * @param key
    *   the key to remove
    * @return
    *   the modified flash scope
    */
  def -(key: Alert): Flash = copy(data - key)

  /** Retrieves the flash value that is associated with the given key.
    */
  def apply(key: Alert): NonEmptyString = data(key)

}
