package uz.scala.messenger.pages

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, CtorType, ScalaComponent}
import org.scalajs.dom.html.Element
import org.scalajs.dom.{HTMLInputElement, window}
import uz.scala.messenger.AjaxImplicits
import uz.scala.messenger.domain.EmailAndPassword

object LoginPage {
  def apply(): LoginPage = new LoginPage()

}
class LoginPage extends AjaxImplicits {
  case class State(email: String = "", password: String = "")

  type AppComponent  = Scala.Component[Unit, State, Backend, CtorType.Nullary]
  type OnInputChange = SyntheticEvent[HTMLInputElement] => Callback

  class Backend($ : Scala.BackendScope[Unit, State]) {

    def onChangeEmail: OnInputChange = e => $.modState(_.copy(email = e.target.value))

    def onChangePassword: OnInputChange = e => $.modState(_.copy(password = e.target.value))

    def login(implicit state: State): Callback =
      post[EmailAndPassword](s"/user/login", EmailAndPassword(state.email, state.password))
        .fail(onError)
        .doneWithoutContent {
          Callback(window.location.href = "/")
        }
        .asCallback

    def render(implicit state: State): VdomTagOf[Element] = {
      <.div(^.cls := "text-center")(
        <.main(^.cls := "form-signin")(
          <.form(
            <.img(
              ^.cls    := "mb-4",
              ^.src    := "/docs/5.1/assets/brand/bootstrap-logo.svg",
              ^.alt    := "",
              ^.width  := "72",
              ^.height := "57"
            ),
            <.h1(^.cls := "h3 mb-3 fw-normal", "Please sign in"),
            <.div(^.cls := "form-floating")(
              <.input(
                ^.`type`      := "email",
                ^.cls         := "form-control",
                ^.id          := "floatingInput",
                ^.placeholder := "name@example.com",
                ^.value       := state.email,
                ^.onChange ==> onChangeEmail
              ),
              <.label(^.`for` := "floatingInput")("Email address")
            ),
            <.div(^.cls := "form-floating")(
              <.input(
                ^.`type`      := "password",
                ^.cls         := "form-control",
                ^.id          := "floatingPassword",
                ^.placeholder := "Password",
                ^.value       := state.password,
                ^.onChange ==> onChangePassword
              ),
              <.label(^.`for` := "floatingPassword")("Password")
            ),
            <.div(^.cls := "checkbox mb-3")(
              <.label(<.input(^.`type` := "checkbox", ^.value := "remember-me"))(
                "Remember me"
              )
            ),
            <.button(^.cls := "w-100 btn btn-lg btn-primary", ^.`type` := "button", ^.onClick --> login)("Sign in"),
            <.p(^.cls := "mt-5 mb-3 text-muted")("© 2017–2021")
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
