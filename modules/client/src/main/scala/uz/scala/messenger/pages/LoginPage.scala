package uz.scala.messenger.pages

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import japgolly.scalajs.react._
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Element
import org.scalajs.dom.{HTMLInputElement, window}
import uz.scala.messenger.AjaxImplicits

object LoginPage {
  def apply(): LoginPage = new LoginPage()

}
class LoginPage extends AjaxImplicits {
  case class State(email: String = "", password: String = "")
  implicit val enc: Encoder[State] = deriveEncoder
  type AppComponent  = Component[Unit, State, Backend, CtorType.Nullary]
  type OnInputChange = SyntheticEvent[HTMLInputElement] => Callback

  class Backend($ : Scala.BackendScope[_, State]) {

    def onChangeEmail: OnInputChange = e => $.modState(_.copy(email = e.target.value))

    def onChangePassword: OnInputChange = e => $.modState(_.copy(password = e.target.value))

    def onClick(implicit state: State): Callback =
      if (state.email.isEmpty)
        Callback.alert("Please enter email address!")
      else if (state.password.isEmpty)
        Callback.alert("Please enter password!")
      else
        post("/user/login", state)
          .fail(onError)
          .doneWithoutContent {
            Callback(window.location.reload(true))
          }
          .asCallback

    def render(implicit state: State): VdomTagOf[Element] = {
      <.div(^.cls := "container")(
        <.div(^.cls := "col-md-4 offset-md-4")(
          <.div(^.cls := "container text-center")(
            <.main(^.cls := "form-signin")(
              <.h1(^.cls := "h3 mb-3 fw-normal", "Please sign in"),
              <.div(^.cls := "form-floating")(
                <.input(
                  ^.`type`      := "email",
                  ^.cls         := "form-control",
                  ^.id          := "email",
                  ^.name        := "email",
                  ^.placeholder := "name@example.com",
                  ^.value       := state.email,
                  ^.onChange ==> onChangeEmail
                ),
                <.label(^.`for` := "email")("Email address")
              ),
              <.div(^.cls := "form-floating")(
                <.input(
                  ^.`type`      := "password",
                  ^.cls         := "form-control",
                  ^.name        := "password",
                  ^.id          := "password",
                  ^.placeholder := "Password",
                  ^.value       := state.password,
                  ^.onChange ==> onChangePassword
                ),
                <.label(^.`for` := "password")("Password")
              ),
              <.div(^.cls := "checkbox mb-3")(
                <.label(<.input(^.`type` := "checkbox", ^.value := "remember-me"))(
                  "Remember me"
                )
              ),
              <.button(^.cls := "w-100 btn btn-lg btn-primary", ^.onClick --> onClick)("Sign in"),
              <.p(^.cls := "mt-5 mb-3 text-muted")("© 2017–2021")
            )
          )
        )
      )
    }

  }

  val Component: AppComponent =
    ScalaComponent
      .builder[Unit]
      .initialState(State())
      .renderBackend[Backend]
      .build

  def component: Unmounted[Unit, State, Backend] = Component()
}
