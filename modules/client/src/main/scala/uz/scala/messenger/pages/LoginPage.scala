package uz.scala.messenger.pages

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.facade.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, CtorType, ScalaComponent}
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.html.Element
import uz.scala.messenger.AjaxImplicits

object LoginPage {
  def apply(): LoginPage = new LoginPage()

}
class LoginPage extends AjaxImplicits {
  case class State(alert: Option[String] = None, email: String = "", password: String = "")

  type AppComponent  = Component[Option[String], State, Backend, CtorType.Props]
  type OnInputChange = SyntheticEvent[HTMLInputElement] => Callback

  class Backend($ : Scala.BackendScope[_, State]) {

    def onChangeEmail: OnInputChange = e => $.modState(_.copy(email = e.target.value))

    def onChangePassword: OnInputChange = e => $.modState(_.copy(password = e.target.value))

    def render(implicit state: State): VdomTagOf[Element] = {
      <.div(^.cls := "text-center")(
        <.main(^.cls := "form-signin")(
          <.form(^.action := "/user/login", ^.method := "POST", ^.encType := "multipart/form-data")(
            <.img(
              ^.cls    := "mb-4",
              ^.src    := "/docs/5.1/assets/brand/bootstrap-logo.svg",
              ^.alt    := "",
              ^.width  := "72",
              ^.height := "57"
            ),
            <.h1(^.cls := "h3 mb-3 fw-normal", "Please sign in"),
            state.alert.fold(TagMod.empty)(alert => <.h4(^.cls := "h3 mb-3 fw-normal text-danger", alert)),
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
            <.button(^.cls := "w-100 btn btn-lg btn-primary", ^.`type` := "submit")("Sign in"),
            <.p(^.cls := "mt-5 mb-3 text-muted")("© 2017–2021")
          )
        )
      )
    }

  }

  val Component: AppComponent =
    ScalaComponent
      .builder[Option[String]]
      .initialStateFromProps(p => State(p))
      .renderBackend[Backend]
      .build

  def component(alert: Option[String] = None): Unmounted[Option[String], State, Backend] = Component(alert)
}
