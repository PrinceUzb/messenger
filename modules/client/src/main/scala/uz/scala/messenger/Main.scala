package uz.scala.messenger
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document
import org.scalajs.dom.html.Element

import java.time.LocalDateTime

object Main extends App {

  case class State(second: String, minute: String, hour: String)

  def currentTime: State = {
    val now = LocalDateTime.now()
    println(now.getSecond.toString, now.getMinute.toString, now.getHour.toString)
    State(now.getSecond.toString, now.getMinute.toString, now.getHour.toString)
  }

  class Backend($ : BackendScope[Unit, State]) {

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

  val Clock = ScalaComponent
    .builder[Unit]
    .initialState(currentTime)
    .renderBackend[Backend]
    .build

  Clock().renderIntoDOM(document.getElementById("app"))

}
