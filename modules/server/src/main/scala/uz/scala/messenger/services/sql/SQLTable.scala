package uz.scala.messenger.services.sql

import skunk.Codec
import skunk.codec.all.uuid
import uz.scala.messenger.types.IsUUID

trait SQLTable {
  def identifier[ID: IsUUID]: Codec[ID] =
    uuid.imap[ID](IsUUID[ID]._UUID.get)(IsUUID[ID]._UUID.apply)
}
