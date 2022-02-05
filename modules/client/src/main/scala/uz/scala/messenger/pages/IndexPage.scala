package uz.scala.messenger.pages

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import org.scalajs.dom.WebSocket
import org.scalajs.dom.html.Element
import uz.scala.messenger.pages.IndexPage.State

import scala.scalajs.js.timers.setTimeout

object IndexPage {
  case class State()

  def apply(): IndexPage = new IndexPage()
}

class IndexPage {
  type AppComponent = Scala.Component[Unit, State, Backend, CtorType.Nullary]

  class Backend($ : Scala.BackendScope[Unit, State]) {
    def messageWS(): Unit = {
      val ws = new WebSocket("ws://localhost:9000/message")
      ws.onopen = _ =>
      ws.onclose = _ =>
        setTimeout(3000)(messageWS())
      ws.onmessage = { e =>
        println(e.data.toString)
      }
      ws.onerror = { e =>
        ws.close()
        println(s"WebSocket Error ${e.toString}")
      }
    }
    messageWS()


    def render(implicit state: State): VdomTagOf[Element] =
      <.div(^.id := "container")(
        <.div(^.cls := "sidebar")(
          <.header(
            <.input(^.`type` := "text", ^.placeholder := "search")
          ),
          <.ul(
            <.li(
              <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg", ^.alt := ""),
              <.div(
                <.h2("Prénom Nom"),
                <.h3(
                  <.span(^.cls := "status orange"),
                  "offline"
                )
              )
            ),
            <.li(
              <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_02.jpg", ^.alt := ""),
              <.div(
                <.h2("Prénom Nom"),
                <.h3(
                  <.span(^.cls := "status green"),
                  "online"
                )
              )
            )
          )
        ),
        <.main(
          <.header(
            <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/chat_avatar_01.jpg", ^.alt := ""),
            <.div(
              <.h2("Chat with Vincent Porter"),
              <.h3("already 1902 messages")
            ),
            <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/ico_star.png", ^.alt := "")
          ),
          <.ul(^.id := "chat")(
            <.li(^.cls := "you")(
              <.div(^.cls    := "entete")(
                <.span(^.cls := "status green"),
                <.h2("Vincent"),
                <.h3("10:12AM, Today")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message")(
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor."
              )
            ),
            <.li(^.cls := "me")(
              <.div(^.cls := "entete")(
                <.h3("10:12AM, Today"),
                <.h2("Vincent"),
                <.span(^.cls := "status blue")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message")(
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor."
              )
            ),
            <.li(^.cls := "me")(
              <.div(^.cls := "entete")(
                <.h3("10:12AM, Today"),
                <.h2("Vincent"),
                <.span(^.cls := "status blue")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message", "OK")
            ),
            <.li(^.cls := "you")(
              <.div(^.cls    := "entete")(
                <.span(^.cls := "status green"),
                <.h2("Vincent"),
                <.h3("10:12AM, Today")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message")(
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor."
              )
            ),
            <.li(^.cls := "me")(
              <.div(^.cls := "entete")(
                <.h3("10:12AM, Today"),
                <.h2("Vincent"),
                <.span(^.cls := "status blue")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message")(
                "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor."
              )
            ),
            <.li(^.cls := "me")(
              <.div(^.cls := "entete")(
                <.h3("10:12AM, Today"),
                <.h2("Vincent"),
                <.span(^.cls := "status blue")
              ),
              <.div(^.cls := "triangle"),
              <.div(^.cls := "message", "OK")
            )
          ),
          <.footer(
            <.textarea(^.placeholder := "Type your message"),
            <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/ico_picture.png", ^.alt := ""),
            <.img(^.src := "https://s3-us-west-2.amazonaws.com/s.cdpn.io/1940306/ico_file.png", ^.alt    := ""),
            <.a(^.href  := "#", "Send")
          )
        )
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
