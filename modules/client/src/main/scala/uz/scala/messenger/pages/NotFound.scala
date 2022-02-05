package uz.scala.messenger.pages

import japgolly.scalajs.react.component.Scala
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{CtorType, ScalaComponent}
import org.scalajs.dom.html.Element

object NotFound {
  def apply(): NotFound = new NotFound()
}

class NotFound {
  case class State()
  type AppComponent = Scala.Component[Unit, State, Backend, CtorType.Nullary]

  class Backend($ : Scala.BackendScope[Unit, State]) {
    def render(implicit state: State): VdomTagOf[Element] = {
      <.div(^.cls := "text-center")(
        <.h1(^.cls := "text-danger")("404"),
        <.h3(^.cls := "text-danger")("Page Not Found")
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
