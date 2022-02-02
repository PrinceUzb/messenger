package uz.scala.messenger.test.utils

import uz.scala.messenger.domain._
import uz.scala.messenger.domain.custom.refinements._
import eu.timepit.refined.auto.autoUnwrap

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random

object FakeData {
  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomEmail: EmailAddress = EmailAddress.unsafeFrom(s"${randomString(8)}@gmail.com")

  val Pass: Password = Password.unsafeFrom("Secret1!")

  def credentials(isCorrect: Boolean): Credentials =
    if (isCorrect)
      Credentials(EmailAddress.unsafeFrom("test@test.test") , Password.unsafeFrom("Secret1!"))
    else
      Credentials(EmailAddress.unsafeFrom(FakeData.randomEmail) , Password.unsafeFrom("Secret1!"))

  def user(email: EmailAddress = randomEmail): User =
    User(
      id = UUID.randomUUID(),
      nickname = Nickname.unsafeFrom("Nickname"),
      email = EmailAddress.unsafeFrom(email),
      createdAt = LocalDateTime.now
    )

  def userData: UserData =
    UserData(
      nickname = Nickname.unsafeFrom("Nickname"),
      email = randomEmail,
      password = Password.unsafeFrom("Secret1!")
    )
}