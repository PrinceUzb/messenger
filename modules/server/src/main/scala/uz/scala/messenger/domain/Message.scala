package uz.scala.messenger.domain

import eu.timepit.refined.types.string.NonEmptyString

import java.util.UUID

case class Message(
  id: UUID,
  to: UUID,
  from: UUID,
  text: NonEmptyString
)
