package us.scala.messenger

import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import uz.scala.messenger.services.Messages
import uz.scala.messenger.test_utils.DBSuite
import uz.scala.messenger.generators._

object MessagesSuite extends DBSuite {
  test("Create message") { implicit postgres =>
    val sendMessage = sendMessageGen(JaneId.some).get
    Messages.make[IO].create(sendMessage, JohnId).map { message =>
      assert.all(
        message.content == sendMessage.content,
        message.receiverId == sendMessage.receiverId,
      )
    }
  }
}
