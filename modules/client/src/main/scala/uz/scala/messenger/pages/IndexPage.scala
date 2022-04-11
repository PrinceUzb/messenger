package uz.scala.messenger.pages

import eu.timepit.refined.types.string.NonEmptyString
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import org.scalajs.dom.html.Element
import org.scalajs.dom.{HTMLInputElement, WebSocket}
import uz.scala.messenger.AjaxImplicits
import uz.scala.messenger.domain.{Message, SendMessage, User}
import uz.scala.messenger.implicits.{CirceDecoderOps, GenericTypeOps}
import uz.scala.messenger.pages.IndexPage.State
import uz.scala.messenger.utils.ldtToStr

import java.time.LocalDateTime
import java.util.UUID
import scala.scalajs.js.timers.setTimeout

object IndexPage {
  case class State(
    user: Option[User] = None,
    users: List[User] = Nil,
    ws: Option[WebSocket] = None,
    selectedUser: Option[User] = None,
    chats: Map[UUID, List[Message]] = Map.empty,
    sendTexts: Map[UUID, String] = Map.empty
  )

  def apply(): IndexPage = new IndexPage()
}

class IndexPage extends AjaxImplicits {
  type AppComponent  = Scala.Component[Unit, State, Backend, CtorType.Nullary]
  type OnInputChange = SyntheticEvent[HTMLInputElement] => Callback

  class Backend($ : Scala.BackendScope[Unit, State]) {
    def messageWS(userID: UUID): Callback = {
      val ws: WebSocket = new WebSocket("ws://localhost:9000/message/" + userID)
      $.modState(_.copy(ws = Option(ws))).map { _ =>
        ws.onclose = _ => setTimeout(3000)(messageWS(userID).runNow())
        ws.onmessage = { e =>
          val message = e.data.toString.as[Message]
          $.modState { s =>
            val histories = s.chats.getOrElse(message.from, List.empty)
            s.copy(chats = s.chats.updated(message.from, message +: histories))
          }.runNow()
        }
        ws.onerror = { e =>
          ws.close()
          println(s"WebSocket Error ${e.toString}")
        }
      }
    }

    def onOpenChat(user: User): Callback =
      $.modState(_.copy(selectedUser = Some(user)))

    def onChangeText(userId: UUID): OnInputChange = e =>
      $.modState(s => s.copy(sendTexts = s.sendTexts.updated(userId, e.target.value)))

    def onSend(userID: UUID)(implicit state: State): Callback =
      state.sendTexts.get(userID).fold(Callback.empty) { text =>
        val ws = state.ws.getOrElse(new WebSocket("ws://localhost:9000/message/" + userID))
        Callback.when(text.nonEmpty) {
          state.user.fold(Callback.empty) { from =>
            val message =
              Message(UUID.randomUUID(), userID, from.id, NonEmptyString.unsafeFrom(text), LocalDateTime.now())
            ws.send(SendMessage(userID, NonEmptyString.unsafeFrom(text)).toJson)
            $.modState { s =>
              val histories = s.chats.getOrElse(userID, List.empty)
              s.copy(chats = s.chats.updated(userID, message +: histories),sendTexts = s.sendTexts.updated(userID, ""))
            }
          }
        }
      }

    def getAllUser: Callback =
      get("/user/all")
        .fail(onError)
        .done[List[User]] { users =>
          $.modState(_.copy(users = users))
        }
        .asCallback

    get("/user")
      .fail(onError)
      .done[User] { user =>
        $.modState(_.copy(user = Some(user))) >> messageWS(user.id) >> getAllUser
      }
      .asCallback
      .runNow()

    def openChat(user: User)(implicit state: State): VdomTagOf[Element] =
      <.main(
        <.header(
          <.img(^.src := "/public/images/avatar_01.jpg", ^.alt := ""),
          <.div(
            <.h2(s"Chat with ${user.nickname}"),
            <.h3(s"already ${state.chats.get(user.id).fold(0)(_.length)} messages")
          ),
          <.img(^.src := "/public/images/ico_star.png", ^.alt := "")
        ),
        <.ul(^.id := "chat")(
          state.chats.getOrElse(user.id, List.empty).reverse.map { message =>
            val isMyMessage = state.user.exists(_.id == message.from)
            val status = List(
              <.span(
                ^.classSet1M(
                  "status",
                  Map("green" -> !isMyMessage, "blue" -> isMyMessage)
                )
              ),
              <.h2(if (isMyMessage) state.user.fold("")(_.nickname.value) else user.nickname.value),
              <.h3(ldtToStr(message.created_at))
            )
            VdomArray(
              <.li(^.classSetM(Map("you" -> !isMyMessage, "me" -> isMyMessage)))(
                <.div(^.cls := "entete")(
                  (if (isMyMessage) status.reverse else status): _*
                ),
                <.div(^.cls := "triangle"),
                <.div(^.cls := "message")(message.text.value)
              )
            )
          }: _*
        ),
        <.footer(
          <.textarea(
            ^.placeholder := "Type your message",
            ^.value       := state.sendTexts.getOrElse(user.id, ""),
            ^.onChange ==> onChangeText(user.id)
          ),
          <.img(^.src := "/public/images/ico_picture.png", ^.alt := ""),
          <.img(^.src := "/public/images/ico_file.png", ^.alt    := ""),
          <.a(^.href := "#", ^.onClick --> onSend(user.id))("Send")
        )
      )

    def render(implicit state: State): VdomTagOf[Element] =
      <.div(^.id := "container")(
        <.div(^.cls := "sidebar")(
          <.header(
            <.input(^.`type` := "text", ^.placeholder := "search")
          ),
          <.ul(
            state.users.map { user =>
              <.li(^.onClick --> onOpenChat(user))(
                <.img(^.src := "/public/images/avatar_01.jpg", ^.alt := ""),
                <.div(
                  <.h2(user.nickname.value),
                  <.h3(
                    <.span(^.cls := "status green"),
                    "online"
                  )
                )
              )
            }: _*
          )
        ),
        state.selectedUser.fold(EmptyVdom)(openChat)
      )
  }

  val Component: AppComponent =
    ScalaComponent
      .builder[Unit]
      .initialState(State())
      .renderBackend[Backend]
      .build

  def component: Unmounted[Unit, State, Backend] = Component()
}
